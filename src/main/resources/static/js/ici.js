var mouseX, mouseY;

const nbGestures = 28;

const  normalizePos = (positions) => {

    // il y a sans doute un meilleur algorithme pour recuper les min/max
    const xMin = positions.sort(function (p1, p2){
        return p1.x < p2.x ? -1 : 1;
    })[0].x;
    const yMin = positions.sort(function (p1, p2){
        return p1.y < p2.y ? -1 : 1;
    })[0].y;
    const xMax = positions.sort(function (p1, p2){
        return p1.x > p2.x ? -1 : 1;
    })[0].x;
    const yMax = positions.sort(function (p1, p2){
        return p1.y > p2.y ? -1 : 1;
    })[0].y;
    console.log("xMin: ", xMin, ", yMin: ", yMin);
    console.log("xMax: ", xMax, ", yMax: ", yMax);

    return positions.map(p => {
        return { x: (p.x-xMin)/(xMax-xMin), y: (p.y-yMin)/(yMax-yMin) };
    })
};

const startGesture = () => {

    const gestures = [];

    var i = 0;
    var interval = setInterval(() => {
        const mx = mouseX;
        const my = mouseY;
        gestures.push({x: mx, y: my});
        ++i;
    }, 100);

    setTimeout(()  =>  {
        clearInterval(interval);
        fetch("/api/mouseGesture", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                positions: normalizePos(gestures),
                shortcut: $('#formGesture input[name="shortcut"]').val()
            })
        }).then(e => {
            e.json().then(json => console.log(json));
        })
    }, 100*nbGestures+1);
    return false;
};

const detectGesture = () => {

    const gestures = [];

    var i = 0;
    var interval = setInterval(() => {
        const mx = mouseX;
        const my = mouseY;
        gestures.push({x: mx, y: my});
        ++i;
    }, 100);

    setTimeout(()  => {
        clearInterval(interval);
        fetch('/api/detect', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                positions: normalizePos(gestures),
                shortcut: "UNKNOW"
            })
        }).then(d => {
            d.text().then(data => {
                console.log(data);
            });
        })
    }, 100*nbGestures+1);
}

$(document).ready(function (){

    const pencilImage = new Image(); // Create new img element
    pencilImage.src = "/assets/spritePencil.png";

    var canvas = $('#canvas').get(0);
    var context = canvas.getContext("2d");

    $('#formGesture').submit(function (e){
        startGesture();
        return false;
    });

    $('#formDetectGesture').submit(function (){
        detectGesture();
        return false;
    });

    $('#saveModelButton').click(function(){
        fetch('/api/applyModel', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(d => {
            console.log("Neural network updated.")
        });
    })

    function getOffset(el) {
        const rect = el.getBoundingClientRect();
        return {
            left: rect.left + window.scrollX,
            top: rect.top + window.scrollY
        };
    }

    $("body").mousemove(function(e) {
        mouseX = e.pageX;
        mouseY = e.pageY;

        if( isDragging ){
            // apply pencil to image
            const offset = getOffset(canvas);

            const rp = { x : mouseX-offset.left, y : mouseY-offset.top };

        }
//        console.log('MouseX', mouseX, ', mouseY', mouseY);
    });

    function renderPencil(ctx, pos){

        const imgFinal = new Image(); // Create new img element

        ctx.fillStyle = "rgb(0, 0, 0)";
        ctx.fillRect(0, 0, $(canvas).width(), $(canvas).height());

    }

    var isDragging = false;
    $('body').mousedown(() => {
        isDragging = true;
    }).mouseup(() => {
        isDragging = false;
    });

    if (!context) {
        console.error("Could not load canvas 2D context");
    }else{
        console.log('Canvas loaded');
    }
});
