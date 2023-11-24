import puppeteer from 'puppeteer';

(async () => {
    const browser = await puppeteer.launch({headless:false});
    const page = await browser.newPage();
    await page.setViewport({width: 1366, height: 768})
    await page.goto('https://chat.europnet.org/?nick=robinouu&age=&sexe=F&ville=Fontenay-aux-Roses&channel=accueil%2Cparis&userid=&origine=9&chatnow=1');

    /*
//check that the first page opened this new page:
    const newTarget = await browser.waitForTarget(target => target.opener() === pageTarget);
//get the new page object:
    const newPage = await newTarget.page();
*/
        await page.waitForTimeout(60*60*1000);

        const texts = await page.$$('.kiwi-messagelist-message-privmsg');
        for(var i = 0; i < texts.length; ++i){
            const text = await (await texts[i].getProperty('textContent')).jsonValue();
            console.log(text)
        }
    // other actions...
    await browser.close();
})();