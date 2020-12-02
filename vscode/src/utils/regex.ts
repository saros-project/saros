export namespace regex {
  const jidPartSuffix = /[a-z0-9.-]+\.[a-z]{2,10}/;
  const jidPartPrefix = new RegExp('[^' +
      [
        /\u0020/,
        /\u0022/,
        /\u0026/,
        /\u0027/,
        /\u002f/,
        /\u003a/,
        /\u003c/,
        /\u003e/,
        /\u0040/,
        /\u007f/,
        /\u0080-\u009f/,
        /\u00a0/,
      ].map((r) => r.source).join('') +
      ']+',
  );
  export const jidPartDivider = '@';
  export const jidSuffix = new RegExp('^' + jidPartSuffix.source + '$');
  export const jidPrefix = new RegExp('^' + jidPartPrefix.source + '$');
  export const jid = new RegExp('^' +
    jidPartPrefix.source +
    jidPartDivider +
    jidPartSuffix.source +
    '$');
}
