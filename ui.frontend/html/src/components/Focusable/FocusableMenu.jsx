import React from 'react';
import Dropdown from 'react-bootstrap/Dropdown';
import { noop } from 'Utils';

export default function FocusableMenu({ children, source, onKeyDownCapture = noop, ...attributes }) {
  return <Dropdown {...attributes} onKeyDownCapture={e => { handleKey(e.key, source); onKeyDownCapture(e); }}>
    {children}
  </Dropdown>
}

function handleKey(key, source) {
  if (key === "Escape" && source) {
    source.focus();
  }
}
