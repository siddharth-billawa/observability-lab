import http from 'k6/http'; import {check} from 'k6'; const base=__ENV.BASE_URL||'http://localhost:18080';
const ramp=Number(__ENV.RAMP_SECONDS||30), burst=Number(__ENV.BURST_SECONDS||30), peak=Number(__ENV.BURST_VUS||15);
export const options={stages:[{duration:`${ramp}s`,target:Math.max(1,Math.floor(peak/5))},{duration:`${burst}s`,target:peak},{duration:`${ramp}s`,target:1}],thresholds:{http_req_failed:['rate<0.15']}};
export default function(){const r=http.post(`${base}/api/orders`,JSON.stringify({item:'burst-widget',amount:19.99}),{headers:{'Content-Type':'application/json'}});check(r,{'created':x=>x.status===201});}
