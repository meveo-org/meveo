
## Serving webpages from meveo
Meveo can be used as a simple web server that will serve webpages by uploading them directly using the Meveo File Explorer.

A webpage with a single file can be uploaded directly, but if the webpage or webapp has multiple files and folders, the easiest way to upload is to zip them and let Meveo unzip them automatically.

To upload files to be served from meveo, follow these steps:
1. Log in to meveo.
2. Select `Execution` menu and click on `File Explorer`.
3. Click the `frontend` folder.
4. In the input box enter the preferred web application's name, do not use spaces in between. (This will be used to refer to it on the URL path.  From here on, it will be referred to as `WEB_APP`).
5. Click the `Create Directory` button.
6. Click the newly created `WEB_APP` folder.
7. If the file to be uploaded is a zip file, select `Unzip automately`
8. Click the `+ Choose` button.
9. Find and select the file to be uploaded then click on `Open`
10. Wait for the upload (and unzip) to be completed.
11.  Open a separate browser window or tab and go to:
```
MEVEO_SERVER_DOMAIN/meveo/frontend/default/WEB_APP/FILE.HTML
```
> `MEVEO_SERVER_DOMAIN` is the name of the domain where meveo is deployed

> `WEB_APP` is the folder where the file was uploaded

> `FILE.HTML` is the web page index or entrypoint
