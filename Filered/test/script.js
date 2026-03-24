const API = "../api/filered.php";

/* ================= MATRIX BACKGROUND ================= */
const canvas = document.getElementById("matrix");
const ctx = canvas.getContext("2d");

canvas.height = window.innerHeight;
canvas.width = window.innerWidth;

const letters = "01";
const fontSize = 14;
const columns = canvas.width / fontSize;

const drops = [];
for(let i=0;i<columns;i++) drops[i]=1;

function draw(){
    ctx.fillStyle="rgba(2,6,23,0.08)";
    ctx.fillRect(0,0,canvas.width,canvas.height);

    ctx.fillStyle="#00ff9c";
    ctx.font=fontSize+"px monospace";

    for(let i=0;i<drops.length;i++){
        const text = letters[Math.floor(Math.random()*letters.length)];
        ctx.fillText(text,i*fontSize,drops[i]*fontSize);

        if(drops[i]*fontSize>canvas.height && Math.random()>0.975){
            drops[i]=0;
        }
        drops[i]++;
    }
}
setInterval(draw,40);

/* ================= LOG ================= */
function log(el,msg,type=""){
    el.innerHTML += `<div class="${type}">> ${msg}</div>`;
    el.scrollTop = el.scrollHeight;
}

/* ================= FILE BOX ================= */
const fileBox = document.getElementById("fileBox");
const fileInputEl = document.getElementById("fileInput");
const fileInfo = document.getElementById("fileInfo");
const filePreview = document.getElementById("filePreview");

/* CLICK */
fileBox.addEventListener("click", () => {
    fileInputEl.click();
});

/* DRAG */
fileBox.addEventListener("dragover", (e) => {
    e.preventDefault();
    fileBox.classList.add("active");
});

fileBox.addEventListener("dragleave", () => {
    fileBox.classList.remove("active");
});

fileBox.addEventListener("drop", (e) => {
    e.preventDefault();
    fileBox.classList.remove("active");

    const file = e.dataTransfer.files[0];
    fileInputEl.files = e.dataTransfer.files;
    handleFile(file);
});

/* CHANGE */
fileInputEl.addEventListener("change", () => {
    const file = fileInputEl.files[0];
    handleFile(file);
});

/* HANDLE FILE */
function handleFile(file){

    if(!file) return;

    fileInfo.innerHTML = `
        > ${file.name} <br>
        > ${(file.size/1024/1024).toFixed(2)} MB
    `;

    filePreview.innerHTML = "";

    const url = URL.createObjectURL(file);

    if(file.type.startsWith("image")){
        filePreview.innerHTML = `<img src="${url}">`;
    }
    else if(file.type.startsWith("video")){
        filePreview.innerHTML = `<video controls src="${url}"></video>`;
    }
    else if(file.type.startsWith("audio")){
        filePreview.innerHTML = `<audio controls src="${url}"></audio>`;
    }
    else if(file.type === "application/pdf"){
        filePreview.innerHTML = `<iframe src="${url}"></iframe>`;
    }
    else{
        filePreview.innerHTML = `<div>> File loaded: ${file.name}</div>`;
    }
}

/* ================= MIME ================= */
function mime(ext){
    return {

        // IMAGES
        jpg:"image/jpeg", jpeg:"image/jpeg", png:"image/png",
        gif:"image/gif", webp:"image/webp", bmp:"image/bmp",
        svg:"image/svg+xml", ico:"image/x-icon", tiff:"image/tiff",

        // VIDEO
        mp4:"video/mp4", avi:"video/x-msvideo", mov:"video/quicktime",
        wmv:"video/x-ms-wmv", flv:"video/x-flv", mkv:"video/x-matroska",
        webm:"video/webm", m4v:"video/x-m4v", "3gp":"video/3gpp",

        // AUDIO
        mp3:"audio/mpeg", wav:"audio/wav", ogg:"audio/ogg",
        aac:"audio/aac", flac:"audio/flac", m4a:"audio/mp4",

        // DOCS
        pdf:"application/pdf", txt:"text/plain",
        rtf:"application/rtf", odt:"application/vnd.oasis.opendocument.text",

        // OFFICE
        doc:"application/msword",
        docx:"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        xls:"application/vnd.ms-excel",
        xlsx:"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        ppt:"application/vnd.ms-powerpoint",
        pptx:"application/vnd.openxmlformats-officedocument.presentationml.presentation",

        // OPENDOC
        ods:"application/vnd.oasis.opendocument.spreadsheet",
        odp:"application/vnd.oasis.opendocument.presentation",

        // COMPRESSED
        zip:"application/zip",
        rar:"application/vnd.rar",
        "7z":"application/x-7z-compressed",
        tar:"application/x-tar",
        gz:"application/gzip",

        // DATA
        json:"application/json",
        xml:"application/xml",
        csv:"text/csv",
        md:"text/markdown",
        log:"text/plain"

    }[ext] || "application/octet-stream";
}

/* ================= UPLOAD ================= */
async function upload(){

    const file = fileInputEl.files[0];
    const logBox = document.getElementById("uploadLog");
    logBox.innerHTML="";

    if(!file) return log(logBox,"No file selected","error");

    log(logBox,"Initializing secure upload...");

    const fd = new FormData();
    fd.append("file",file);
    fd.append("action","addFile");

    try{
        const res = await fetch(API,{method:"POST",body:fd});
        const json = await res.json();

        if(json.validacion==="Exitoso"){
            log(logBox,"Upload success","success");
            log(logBox,"ID: "+json.data.id);
            log(logBox,"EXT: "+json.data.extension);
            log(logBox,"TOKEN: "+json.data.token);
        }else{
            throw json.mensaje;
        }

    }catch(e){
        log(logBox,"Error: "+e,"error");
    }
}

/* ================= GET ================= */
async function getFile(){

    const id = document.getElementById("fileId").value;
    const ext = document.getElementById("fileExt").value.toLowerCase();
    const token = document.getElementById("fileToken").value;

    const logBox = document.getElementById("getLog");
    const preview = document.getElementById("preview");

    logBox.innerHTML="";
    preview.innerHTML="";

    if(!id||!ext||!token){
        return log(logBox,"Missing parameters","error");
    }

    log(logBox,"Requesting asset...");

    try{

        const fd = new FormData();
        fd.append("action","getFile");
        fd.append("id",id);
        fd.append("extension",ext);
        fd.append("token",token);

        const res = await fetch(API,{
            method:"POST",
            body:fd
        });

        const blob = await res.blob();

        if(blob.type.includes("text") || blob.type.includes("json")){
            const txt = await blob.text();
            throw txt;
        }

        const url = URL.createObjectURL(blob);

        log(logBox,"Access granted","success");

        if(ext.match(/png|jpg|jpeg|webp/)){
            preview.innerHTML=`<img src="${url}">`;
        }
        else if(ext.match(/mp4|webm/)){
            preview.innerHTML=`<video controls src="${url}"></video>`;
        }
        else if(ext.match(/mp3/)){
            preview.innerHTML=`<audio controls src="${url}"></audio>`;
        }
        else if(ext==="pdf"){
            preview.innerHTML=`
                <iframe src="${url}" width="100%" height="500"></iframe>
                <br>
                <a href="${url}" download>Descargar PDF</a>
            `;
        }
        else{
            preview.innerHTML=`<a href="${url}" download>Download File</a>`;
        }

    }catch(e){
        log(logBox,"Access denied: "+e,"error");
    }
}