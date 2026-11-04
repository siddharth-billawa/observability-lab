import http from 'k6/http'; import {check, sleep} from 'k6';
const base=__ENV.BASE_URL||'http://localhost:18080';
export const options={vus:Number(__ENV.VUS||3),duration:__ENV.DURATION||'2m', thresholds:{http_req_failed:['rate<0.05']}};
export default function(){const r=http.post(`${base}/api/orders`,JSON.stringify({item:'demo-widget',amount:12.50}),{headers:{'Content-Type':'application/json'}});check(r,{'created':x=>x.status===201});sleep(.2);}
