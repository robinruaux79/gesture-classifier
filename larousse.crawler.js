import puppeteer from 'puppeteer';
import fs from "fs";

(async () => {

    const words = fs.readFileSync('data/words.txt').toString("UTF-8").split("\r\n");
    console.log(words);

    const browser = await puppeteer.launch({headless:false});

    for(var i = 0; i < words.length; ++i) {
        const page = await browser.newPage();
        await page.setViewport({width: 1366, height: 768});
        await page.goto('https://www.larousse.fr/');

        try {
            await page.waitForSelector("#onetrust-accept-btn-handler", {timeout: 5000});
            await page.evaluate(() => {
                document.querySelector('#onetrust-accept-btn-handler').click();
            });
        } catch (e){

        }

        // do search
        await page.$$('.lar-searchtxt');
        await page.type('.lar-searchtxt', words[i]);
        await page.click(".lar-searchbt");


        // terminate session
        await page.close();
    }
    // other actions...
    await browser.close();
})();