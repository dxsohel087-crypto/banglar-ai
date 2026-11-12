<!DOCTYPE html>
<html lang="bn">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="theme-color" content="#07130f">
<title>বাংলার AI - Futuristic</title>

<style>
*{margin:0;padding:0;box-sizing:border-box}

:root{
 --bg:#050e0a;
 --panel:#10221a;
 --green:#35ed91;
 --text:#eafff1;
 --muted:#91aa9a;
}

body{
 background:radial-gradient(circle at top,#123b28,#050e0a 65%);
 color:var(--text);
 font-family:Arial,sans-serif;
 min-height:100vh;
 padding:15px;
}

.app{
 max-width:480px;
 margin:auto;
 min-height:95vh;
 display:flex;
 flex-direction:column;
}

header{
 display:flex;
 justify-content:space-between;
 align-items:center;
 padding:12px 4px;
}

.logo{
 font-size:24px;
 font-weight:900;
 color:var(--green);
}

.version{
 background:#173b29;
 color:var(--green);
 padding:7px 12px;
 border-radius:20px;
 font-size:11px;
}

.subtitle{
 color:var(--muted);
 font-size:11px;
 letter-spacing:2px;
 margin-top:5px;
}

.orb-area{
 height:245px;
 display:flex;
 align-items:center;
 justify-content:center;
 position:relative;
}

.orb{
 width:165px;
 height:165px;
 border-radius:50%;
 background:radial-gradient(circle at 30% 25%,
 #fff 0%,#a5ffca 12%,#3bed8c 35%,
 #087d45 65%,#022b17 100%);
 box-shadow:0 0 30px #35ed9166,
 0 0 80px #35ed9133,
 inset -15px -15px 30px #00351b;
 display:flex;
 justify-content:center;
 align-items:center;
 animation:float 3s infinite ease-in-out;
}

.orb-core{
 width:55px;
 height:55px;
 border-radius:50%;
 background:#ffffffc9;
 box-shadow:0 0 40px white;
}

.orb.thinking{
 animation:pulse .6s infinite alternate;
}

.orb.listening{
 animation:pulse 1s infinite alternate;
 background:radial-gradient(circle at 30% 25%,
 #fff 0%,#ffc5a5 12%,#ed7a3b 35%,
 #8d3108 65%,#2b0c02 100%);
 box-shadow:0 0 30px #ed7a3b66,
 0 0 80px #ed7a3b33,
 inset -15px -15px 30px #351300;
}

@keyframes float{
 50%{transform:translateY(-8px)}
}

@keyframes pulse{
 to{transform:scale(1.12);filter:brightness(1.3)}
}

.status{
 text-align:center;
 color:var(--green);
 font-size:14px;
 font-weight:bold;
 margin-bottom:13px;
}

.card{
 background:#10221acc;
 border:1px solid #244a35;
 border-radius:17px;
 padding:15px;
 margin-bottom:12px;
}

.card-title{
 color:var(--green);
 font-size:11px;
 font-weight:bold;
 letter-spacing:1.5px;
 margin-bottom:10px;
}

.chat{
 height:220px;
 overflow-y:auto;
 display:flex;
 flex-direction:column;
 gap:10px;
}

.bubble{
 padding:11px 13px;
 border-radius:13px;
 font-size:13px;
 line-height:1.6;
 max-width:90%;
 overflow-wrap:anywhere;
 white-space:pre-wrap;
}

.user{
 align-self:flex-end;
 background:#176b42;
}

.ai{
 align-self:flex-start;
 background:#1b3025;
 border:1px solid #315b40;
}

.input-area{
 display:flex;
 gap:8px;
 margin-bottom:12px;
}

input{
 min-width:0;
 flex:1;
 background:#14281d;
 border:1px solid #31543c;
 border-radius:12px;
 padding:14px;
 color:white;
 outline:none;
 font-size:14px;
}

button{
 border:none;
 cursor:pointer;
 border-radius:12px;
 padding:12px;
 font-weight:bold;
}

.send{
 background:var(--green);
 color:#042011;
 font-size:18px;
}

.mic{
 background:#17452e;
 color:var(--green);
 font-size:18px;
}

.controls{
 display:grid;
 grid-template-columns:repeat(3,1fr);
 gap:9px;
}

.control{
 background:#152a1e;
 color:#c9f9d9;
 border:1px solid #31543c;
 font-size:12px;
 min-height:60px;
}

.control:active,button:active{
 transform:scale(.96);
}

.power{
 display:flex;
 gap:10px;
 margin-bottom:12px;
}

.power button{
 flex:1;
 padding:14px;
}

.on{
 background:#18c875;
 color:#042011;
}

.off{
 background:#293b31;
 color:white;
}

.footer{
 text-align:center;
 color:#607c69;
 font-size:10px;
 margin-top:auto;
 padding:20px 0;
}

.settings{
 display:none;
}

.clear{
 background:#402321;
 color:#ffb7a9;
 width:100%;
 margin-top:10px;
}
</style>
</head>

<body>

<div class="app">

<header>
 <div>
  <div class="logo">✦ বাংলার AI</div>
  <div class="subtitle">FUTURISTIC PERSONAL ASSISTANT</div>
 </div>
 <div class="version">GROQ AI 1.0</div>
</header>

<div class="orb-area">
 <div class="orb" id="orb">
  <div class="orb-core"></div>
 </div>
</div>

<div class="status" id="status">● AI বন্ধ আছে</div>

<div class="power">
 <button class="on" onclick="turnOn()">● ON</button>
 <button class="off" onclick="turnOff()">■ OFF</button>
 </div>

<div class="card">
 <div class="card-title">AI CONVERSATION</div>
 <div class="chat" id="chat">
  <div class="bubble ai">
   আসসালামু আলাইকুম! আমি বাংলার AI।
   আমাকে যেকোনো প্রশ্ন করতে পারেন।
  </div>
 </div>
</div>

<div class="input-area">
 <input id="userInput" placeholder="আপনার নির্দেশ লিখুন..."
 autocomplete="off">
 <button class="send" onclick="sendText()">➤</button>
 <button class="mic" onclick="startVoiceManual()">🎙</button>
</div>

<div class="card">
 <div class="card-title">SMART MOBILE CONTROL</div>

 <div class="controls">
  <button class="control" onclick="mobileAction('wifi')">📶<br>Wi-Fi</button>
  <button class="control" onclick="mobileAction('bluetooth')">ᛒ<br>Bluetooth</button>
  <button class="control" onclick="mobileAction('flash')">🔦<br>Flashlight</button>
  <button class="control" onclick="mobileAction('camera')">📷<br>Camera</button>
  <button class="control" onclick="mobileAction('youtube')">▶<br>YouTube</button>
  <button class="control" onclick="mobileAction('settings')">⚙<br>Settings</button>
  <button class="control" onclick="mobileAction('facebook')">f<br>Facebook</button>
  <button class="control" onclick="mobileAction('gallery')">🖼<br>Gallery</button>
  <button class="control" onclick="mobileAction('brightness')">☀<br>Brightness</button>
 </div>
</div>

<div class="card">
 <div class="card-title">AI SETTINGS</div>
 <button class="control" style="width:100%"
 onclick="toggleSettings()">⚙ চ্যাট ইতিহাস মুছুন বা সেটিংস</button>

 <div class="settings" id="settings">
  <button class="clear" onclick="clearHistory()">
   Clear Chat History
  </button>
  <p class="small" style="margin-top:10px; color:var(--muted); font-size:11px;">
   API Key সফলভাবে কোডের সাথে যুক্ত করা আছে।
  </p>
 </div>
</div>

<div class="footer">
 BANGLAR AI • POWERED BY GROQ<br>
 MADE FOR SOHEL
</div>

</div>

<script>

const MODEL = "openai/gpt-oss-120b";
const API_KEY = "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";

let isOn = false;
let busy = false;
let recognition = null;
let isListening = false;
let torchStream = null;
let torchOn = false;

const orb = document.getElementById("orb");
const status = document.getElementById("status");
const chat = document.getElementById("chat");
const userInput = document.getElementById("userInput");

function addMessage(text,type){
 const div = document.createElement("div");
 div.className = "bubble " + type;
 div.textContent = text;
 chat.appendChild(div);
 chat.scrollTop = chat.scrollHeight;
}

function clearHistory(){
 chat.innerHTML = "";
 addMessage("নতুন কথোপকথন শুরু হয়েছে।","ai");
}

function setStatus(text){
 status.innerText = text;
}

function toggleSettings(){
 const s = document.getElementById("settings");
 s.style.display = s.style.display === "block" ? "none" : "block";
}

// ==================================
// AUTOMATIC CONTINUOUS VOICE LOOP (CALL STYLE)
// ==================================

function initSpeechRecognition() {
  const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SR) return null;
  
  const rec = new SR();
  rec.lang = "bn-BD";
  rec.continuous = false;
  rec.interimResults = false;

  rec.onstart = () => {
    isListening = true;
    orb.classList.remove("thinking");
    orb.classList.add("listening");
    setStatus("🎙 আপনার কথা শুনছি... বলুন...");
  };

  rec.onresult = async (event) => {
    const text = event.results[0][0].transcript.trim();
    if(text) {
      userInput.value = text;
      addMessage(text, "user");
      
      stopListening();

      const handled = await runCommand(text);
      if(!handled) {
        await askAI(text);
      }
    }
  };

  rec.onerror = (event) => {
    isListening = false;
    if(event.error !== "no-speech" && event.error !== "aborted") {
      setStatus("ভয়েস সমস্যা: " + event.error);
    }
  };

  rec.onend = () => {
    isListening = false;
    orb.classList.remove("listening");
    if (isOn && !busy) {
      setTimeout(() => {
        startListeningAuto();
      }, 500);
    }
  };

  return rec;
}

function startListeningAuto() {
  if (!isOn || busy || isListening) return;
  if (!recognition) recognition = initSpeechRecognition();
  
  if (recognition) {
    try {
      recognition.start();
    } catch(e) {}
  }
}

function stopListening() {
  if (recognition) {
    try { recognition.abort(); } catch(e) {}
  }
  isListening = false;
  orb.classList.remove("listening");
}

function startVoiceManual() {
  if(!isOn){
    setStatus("আগে AI চালু করুন");
    return;
  }
  startListeningAuto();
}

// ==================================
// VOICE OUTPUT (TEXT TO SPEECH) & AUTO LOOP RESUME
// ==================================

function speak(text){
  if(!("speechSynthesis" in window)) {
    resumeListeningAfterSpeech();
    return;
  }

  window.speechSynthesis.cancel();
  const speech = new SpeechSynthesisUtterance(text);
  speech.lang = "bn-BD";
  speech.rate = 0.90;
  speech.pitch = 1;

  speech.onend = () => {
    resumeListeningAfterSpeech();
  };

  speech.onerror = () => {
    resumeListeningAfterSpeech();
  };

  window.speechSynthesis.speak(speech);
}

function resumeListeningAfterSpeech() {
  if(isOn && !busy) {
    setStatus("🎙 আপনার কথা শুনছি...");
    startListeningAuto();
  }
}

// ==================================
// AI POWER ON / OFF
// ==================================

function turnOn(){
  isOn = true;
  busy = false;
  setStatus("● বাংলার AI কল চালু হয়েছে");
  speak("আসসালামু আলাইকুম। আমি বাংলার AI। বলুন, কী সাহায্য করতে পারি?");
}

function turnOff(){
  isOn = false;
  busy = false;
  stopListening();
  if("speechSynthesis" in window){
    window.speechSynthesis.cancel();
  }
  orb.classList.remove("thinking", "listening");
  setStatus("● AI বন্ধ আছে");
}

// ==================================
// LOCAL MOBILE COMMANDS
// ==================================

async function mobileAction(action){
  try{
    switch(action){
      case "wifi":
        window.location.href = "intent:#Intent;action=android.settings.WIFI_SETTINGS;end";
        return;
      case "bluetooth":
        window.location.href = "intent:#Intent;action=android.settings.BLUETOOTH_SETTINGS;end";
        return;
      case "settings":
        window.location.href = "intent:#Intent;action=android.settings.SETTINGS;end";
        return;
      case "camera":
        window.location.href = "intent:#Intent;action=android.media.action.IMAGE_CAPTURE;end";
        return;
      case "youtube":
        window.location.href = "https://www.youtube.com/";
        return;
      case "facebook":
        window.location.href = "https://www.facebook.com/";
        return;
      case "gallery":
        window.location.href = "https://photos.google.com/";
        return;
      case "brightness":
        window.location.href = "intent:#Intent;action=android.settings.DISPLAY_SETTINGS;end";
        return;
      case "flash":
        await toggleFlash();
        return;
    }
  }catch(error){
    addMessage("এই কাজটি ব্রাউজার থেকে করা সম্ভব হয়নি।","ai");
  }
}

async function toggleFlash(){
  try{
    if(!torchStream){
      torchStream = await navigator.mediaDevices.getUserMedia({
        video:{facingMode:"environment"}
      });
    }
    const track = torchStream.getVideoTracks()[0];
    const capabilities = track.getCapabilities();
    if(!capabilities.torch){
      addMessage("এই ব্রাউজার বা ক্যামেরায় Flashlight API সমর্থিত নয়।","ai");
      speak("এই ক্যামেরায় ফ্ল্যাশলাইট সমর্থিত নয়।");
      return;
    }
    torchOn = !torchOn;
    await track.applyConstraints({
      advanced:[{torch:torchOn}]
    });
    const msg = torchOn ? "Flashlight চালু হয়েছে।" : "Flashlight বন্ধ হয়েছে.";
    addMessage(msg, "ai");
    speak(msg);
  }catch(error){
    addMessage("Flashlight-এর জন্য ক্যামেরা অনুমতি দিন।","ai");
  }
}

async function runCommand(text){
  const q = text.toLowerCase();
  if(q.includes("ফ্ল্যাশ") || q.includes("টর্চ") || q.includes("flashlight")){
    await toggleFlash();
    return true;
  }
  if(q.includes("ওয়াইফাই") || q.includes("ওয়াইফাই") || q.includes("wifi")){
    await mobileAction("wifi");
    speak("ওয়াইফাই সেটিংস খোলা হচ্ছে।");
    return true;
  }
  if(q.includes("ব্লুটুথ") || q.includes("bluetooth")){
    await mobileAction("bluetooth");
    speak("ব্লুটুথ সেটিংস খোলা হচ্ছে।");
    return true;
  }
  if(q.includes("ইউটিউব") || q.includes("youtube")){
    await mobileAction("youtube");
    speak("ইউটিউব ওপেন করা হচ্ছে।");
    return true;
  }
  if(q.includes("ক্যামেরা") || q.includes("camera")){
    await mobileAction("camera");
    speak("ক্যামেরা চালু করা হচ্ছে।");
    return true;
  }
  if(q.includes("ফেসবুক") || q.includes("facebook")){
    await mobileAction("facebook");
    speak("ফেসবুক ওপেন করা হচ্ছে।");
    return true;
  }
  if(q.includes("গ্যালারি") || q.includes("gallery")){
    await mobileAction("gallery");
    speak("গ্যালারি খোলা হচ্ছে।");
    return true;
  }
  return false;
}

// ==================================
// GROQ AI
// ==================================

async function askAI(question){
  stopListening();
  orb.classList.add("thinking");
  setStatus("AI চিন্তা করছে...");
  busy = true;

  try{
    const response = await fetch(
      "https://api.groq.com/openai/v1/chat/completions",
      {
        method:"POST",
        headers:{
          "Content-Type":"application/json",
          "Authorization":"Bearer "+API_KEY
        },
        body:JSON.stringify({
          model: MODEL,
          messages:[
            {
              role:"system",
              content:"তুমি বাংলার AI। তুমি সোহেল ভাইয়ের ব্যক্তিগত বাংলা AI সহকারী। সহজ, সুন্দর, স্বাভাবিক বাংলায় সংক্ষিপ্ত উত্তর দেবে। তোমার নাম বাংলার AI। তোমাকে কে বানিয়েছে জিজ্ঞেস করলে বলবে, 'আমাকে দিনাজপুরের বোচাগঞ্জ উপজেলার মোল্লাপাড়ার সোহেল ভাই তৈরি করেছেন।' তুমি কোনো ফোনের কাজ সম্পন্ন হয়েছে বলে দাবি করবে না যদি সেটি বাস্তবে করা না হয়।"
            },
            {
              role:"user",
              content: question
            }
          ],
          temperature:0.7,
          max_tokens:1000
        })
      }
    );

    const data = await response.json();
    if(!response.ok){
      throw new Error(data.error?.message || "HTTP "+response.status);
    }

    const answer = data.choices?.[0]?.message?.content;
    if(!answer) throw new Error("AI উত্তর পাওয়া যায়নি");

    addMessage(answer, "ai");
    setStatus("AI উত্তর দিচ্ছে...");
    speak(answer);

  }catch(error){
    const err = "দুঃখিত, সংযোগে সমস্যা হয়েছে।";
    addMessage(err, "ai");
    speak(err);
  }finally{
    busy = false;
    orb.classList.remove("thinking");
  }
}

// ==================================
// MANUAL SEND TEXT
// ==================================

async function sendText(){
  const text = userInput.value.trim();
  if(!text || busy) return;
  if(!isOn){
    addMessage("আগে ON বাটনে চাপ দিন।","ai");
    return;
  }

  userInput.value = "";
  addMessage(text, "user");
  stopListening();

  const handled = await runCommand(text);
  if(!handled) {
    await askAI(text);
  }
}

userInput.addEventListener("keydown", e => {
  if(e.key === "Enter"){
    e.preventDefault();
    sendText();
  }
});

addEventListener("beforeunload", () => {
  if(torchStream){
    torchStream.getTracks().forEach(track => track.stop());
  }
});

</script>

</body>
</html>
