var gameCanvas, persons, player;

// Create WebSocket connection.
const socket = new WebSocket("ws://localhost:8080/ws");

let account;

function hash(string) {
    const utf8 = new TextEncoder().encode(string);
    return crypto.subtle.digest('SHA-256', utf8).then((hashBuffer) => {
        const hashArray = Array.from(new Uint8Array(hashBuffer));
        const hashHex = hashArray
            .map((bytes) => bytes.toString(16).padStart(2, '0'))
            .join('');
        return hashHex;
    });
}

// Connection opened
socket.addEventListener("open", (event) => {
    socket.send(JSON.stringify({ "ok": "google", "test": true, "toto": 1.456788}));
});

$('form#requester').on('submit', function (e){
    var request = $('#request').text();
    var containsProps = (str) => {
        return str.match(/(k|qu)i contient|avec( *(comme|pour) *param[eè]tres?)?|(k|qu)i *a *l[é|es] *propri[eé]t[eé]s? *suivantes?/ui);
    }
    var props = (str) => {
        const regex = /(\S*)\s*=\s*("([^"]*)"|(\S*))/ug;
        let m;
        const props = {};
        do {
            m = regex.exec(str);
            if( m ){
                const value = m[3] !== undefined ? m[3] : m[2];
                const path = m[1].split(".");
                let n = props;
                let p = 0;
                for(; p < path.length-1; ++p){
                    if( n[path[p]] === undefined ){
                        n[path[p]] = {};
                    }
                    n = n[path[p]];
                }
                n[path[p]] = jsonValue(value);
            }
        } while (m);
        return props;
    }
    function isInt(n) {
        return n % 1 === 0;
    }
    const jsonValue = (str) => {

        let val = parseFloat(str.replace(',', '.'));
        if( isInt(val) )
            return Math.floor(val);
        else if (isNaN(val)){
            if( str === "true")
                val = true;
            else if( str==="false")
                val = false;
            else
                val = str;
        }
        return val;
    }

    let p = containsProps(request) ? props(request) : {};
    const requests = {
        "CREATE_GAME" : /(g[eé]n[eè]rer?|crée?r?) *un *(jeu|monde)/,
        "EDIT_GAME": /[ée]dite?r? *le *jeu/,
        "LEAVE_GAME": /(fermer?|quitt?e?r?|arr[êe]tt?er?|stop(per?)?) *le *jeu/,
        "JOIN_GAME": /(acc[eé]der? *au|lancer? *le|entrer *dans *le) *jeu|participer|commencer/,
        "CREATE_GAMEOBJECT": /(met(s|tre)|placer?|ajouter?|g[eé]n[eè]rer?|crée?r?)((sur *le|dans *le|au) *jeu)? *un *objet/
    };

    Object.keys(requests).forEach(async k => {
        var m = requests[k];
        if( request.match(m)){
            const props = p;
            socket.send(JSON.stringify({ type: k, parameters: {
                account,
                object:props
            } }))
        }
    });
    return false;
});

// Listen for messages
socket.addEventListener("message", (event) => {
    //console.log("Message from server ", event.data);
    const json = JSON.parse(event.data);
    if( !persons && json.persons ) {
        persons = json.persons;
    }
    if( json.player ){
        player = json.player;
        playerX = json.player.location.x;
        playerY = json.player.location.y;
    }
    if( json.actions ){
        Object.keys(json.actions).forEach(a => {
            const person = persons.find(x=>x.id === parseInt(a,10))
            const acts = json.actions[a];
            acts.forEach(act => {
                persons = persons.map(p => {
                    if( p.id === person.id){
                        p = doAction(person, act);
                    }
                    return p;
                });
            });
        })
    }
});

function doAction(person, act){
    const dist = 15;
    const p = { x : person.location.x, y: person.location.y };
    if( act.type === "LEFT"){
        p.x -= dist;
    }else if( act.type === "RIGHT"){
        p.x += dist;
    } else if( act.type === "TOP"){
        p.y -= dist;
    } else if( act.type === "BOTTOM"){
        p.y += dist;
    }
    const ploc = { x: person.location.x, y: person.location.y };
    const speed = 1;
    const frames = Math.floor(speed * 24);
    var nbFrames = 0;
    clearInterval(person.moveInterval);
    person.moveInterval = setInterval(function (){
        person.location.x = lerp(ploc.x, p.x, nbFrames / frames);
        person.location.y = lerp(ploc.y, p.y, nbFrames / frames);
        nbFrames++;
        if( nbFrames > frames ){
            clearInterval(person.moveInterval);
        }
    }, 1000/24);

    return person;
}
const debounce = (callback, wait) => {
    let timeoutId = null;
    return (...args) => {
        window.clearTimeout(timeoutId);
        timeoutId = window.setTimeout(() => {
            callback.apply(null, args);
        }, wait);
    };
}
var playerX = 0, playerY = 0;
var horizontal = 0, vertical = 0;

function drawUpdate(){
    var ctx = gameCanvas.getContext("2d");

    ctx.globalCompositeOperation = "destination-over";
    ctx.clearRect(0, 0, 800, 600);
    ctx.save();

    ctx.translate(-playerX, -playerY);

    for(var i = 0; i < persons?.length; ++i) {
        var p = persons[i];
        ctx.fillStyle = "red";
        ctx.fillRect(p.location.x - 5, p.location.y - 5, 10, 10);
    }

    ctx.restore();

    //ctx.translate(playerX, playerY);
    ctx.fillStyle = "green";
    ctx.fillRect(405, 305, 10, 10);


    setTimeout(() => {
        requestAnimationFrame(drawUpdate);
    }, 1000/60);
}


var lastKey = null;
var nbkeys = 0;

$(document).keydown(function (e){
    let type = null;
    if( lastKey === e.key)
        return;
    nbkeys++;
    lastKey = e.key;
    if( e.key === "ArrowLeft") {
        horizontal = -1;
        type = "LEFT";
    } else if( e.key === "ArrowRight") {
        horizontal = 1;
        type= "RIGHT";
    } else if( e.key === "ArrowUp") {
        vertical = -1;
        type = "TOP";
    } else if( e.key === "ArrowDown") {
        vertical = 1;
        type = "BOTTOM";
    }

    if( vertical < 0 ){
        playerY = playerY+ratio;
        doAction(player.character, { type: "TOP"});
    }else if( vertical > 0){
        playerY = playerY-ratio;
        doAction(player.character, { type: "BOTTOM"});
    }else if( horizontal < 0){
        playerX = playerX+ratio;
        doAction(player.F, { type: "LEFT"});
    }else if( horizontal > 0){
        playerX = playerX-ratio;
        doAction(player.character, { type: "RIGHT"});
    }

    if( type && (vertical || horizontal)){
        socket.send(JSON.stringify({ type, parameters: {
                account,
                object : { }
            } }));
    }

});

var lastTime;
setInterval(() =>{

    var now = new Date();
    lastTime = new Date();

}, 1000/60);
function lerp(start_value, end_value, pct)
{
    return (start_value + (end_value - start_value) * pct);
}
$(document).keyup(function (e){
    lastKey = null;
    nbkeys--;
    if( e.key === "ArrowLeft") {
        horizontal = 0;
    } else if( e.key === "ArrowRight") {
        horizontal = 0;
    } else if( e.key === "ArrowUp") {
        vertical = 0;
    } else if( e.key === "ArrowDown") {
        vertical = 0;
    }
    socket.send(JSON.stringify({ type:"NOP", parameters: { account } }));
});

var requestAnimationFrame = window.requestAnimationFrame ||
    window.mozRequestAnimationFrame ||
    window.webkitRequestAnimationFrame ||
    window.msRequestAnimationFrame;

$(document).ready(function (){
    gameCanvas = $("#gameCanvas").get(0);
    (async () => {
        account = await hash('robinouu');
        requestAnimationFrame(drawUpdate);
    })();
});
