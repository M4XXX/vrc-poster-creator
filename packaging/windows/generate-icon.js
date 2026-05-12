// Generates a 32x32 placeholder app-icon.ico (solid purple, BGRA #500050)
// Run once: node generate-icon.js
// Replace output with a real multi-resolution ICO before public release.
const fs = require('fs'), path = require('path');
const W = 32, H = 32, BPP = 32;
const pixelData = Buffer.alloc(W * H * 4);
for (let i = 0; i < W * H; i++) {
  pixelData[i * 4 + 0] = 0xa0; // B
  pixelData[i * 4 + 1] = 0x00; // G
  pixelData[i * 4 + 2] = 0x50; // R
  pixelData[i * 4 + 3] = 0xff; // A
}
const andMask = Buffer.alloc(H * Math.ceil(W / 32) * 4);
const bmpHeader = Buffer.alloc(40);
bmpHeader.writeUInt32LE(40, 0);
bmpHeader.writeInt32LE(W, 4);
bmpHeader.writeInt32LE(H * 2, 8);
bmpHeader.writeUInt16LE(1, 12);
bmpHeader.writeUInt16LE(BPP, 14);
const imgData = Buffer.concat([bmpHeader, pixelData, andMask]);
const icoHeader = Buffer.from([0,0, 1,0, 1,0]);
const dirEntry = Buffer.alloc(16);
dirEntry[0] = W; dirEntry[1] = H;
dirEntry.writeUInt16LE(1, 4);
dirEntry.writeUInt16LE(BPP, 6);
dirEntry.writeUInt32LE(imgData.length, 8);
dirEntry.writeUInt32LE(icoHeader.length + dirEntry.length, 12);
fs.writeFileSync(path.join(__dirname, 'app-icon.ico'), Buffer.concat([icoHeader, dirEntry, imgData]));
console.log('app-icon.ico created (' + (icoHeader.length + dirEntry.length + imgData.length) + ' bytes)');
