const urls = [
    "/content/dam/example/asset1.jpg",
    "/content/dam/example/asset2.jpg",
    "/content/dam/example/asset3.jpg",
    "/content/dam/example/asset4.jpg",
    "/content/dam/example/asset5.jpg"
];

const endpoint = "/bin/asynccommand";

async function getCsrfToken() {
    const res = await fetch("/libs/granite/csrf/token.json", {
        credentials: "include"
    });
    const data = await res.json();
    return data.token;
}

async function processAsset(asset, index, csrfToken) {
    const ts = Date.now() + index;

    const body =
        "processType=full&operation=PROCESS" +
        `&description=${encodeURIComponent(`Process-${asset}-${ts}`)}` +
        `&asset=${encodeURIComponent(asset)}`;

    const res = await fetch(endpoint, {
        method: "POST",
        headers: {
            "accept": "*/*",
            "content-type": "application/x-www-form-urlencoded; charset=UTF-8",
            "csrf-token": csrfToken,
            "x-requested-with": "XMLHttpRequest"
        },
        body,
        credentials: "include"
    });

    if (!res.ok) {
        console.error(`❌ Failed ${asset}`, res.status);
        return;
    }

    console.log(`✅ ${index + 1}/${urls.length} ${asset}`);
}

async function run() {
    const csrfToken = await getCsrfToken();
    console.log("CSRF ready");

    for (let i = 0; i < urls.length; i++) {
        await processAsset(urls[i], i, csrfToken);
        await new Promise(r => setTimeout(r, 200));
    }

    console.log("Done");
}

run();
