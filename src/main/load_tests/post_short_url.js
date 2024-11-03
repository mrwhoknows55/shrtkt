import http from 'k6/http'
import {check, sleep} from 'k6'

const port = 8080
const vus = 330

function getRandomNumber(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export let options = {
    stages: [{duration: '20s', target: vus},
        {duration: '1m20s', target: vus},
        {duration: '20s', target: 0},
    ], thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};
export default function () {
    const randomNum = getRandomNumber(1, vus);
    const url = `http://0.0.0.0:${port}/shorten?url=https://hello.world/${randomNum}`;
    let response = http.post(url);

    // Check that the response is successful
    check(response, {
        'status is 201': (r) => r.status === 201
    });

    sleep(1);
}
