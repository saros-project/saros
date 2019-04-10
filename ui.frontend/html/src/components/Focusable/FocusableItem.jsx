import DropdownItem from 'react-bootstrap/DropdownItem';
import React from 'react';
import { noop } from 'Utils';

/**
 * this item receives focus when the mouse enters it and detects key presses to give focus to other
 * FocusableItem elements
 * @param {{children:React.Component, onMouseEnter: Function, onKeyDown: Function, attributes: any}} param0
 */
export default function FocusableItem({ children, onMouseEnter = noop, onKeyDown = noop, ...attributes }) {
  /**
   * since event handlers are beign used here, the same event handlers are parameterized
   * to allow the execution of both the behaviour defined here and whatever the caller wants
   */
  return (
    <DropdownItem tabIndex="0" draggable="false" {...attributes}
      onMouseEnter={e => { e.currentTarget.focus(); onMouseEnter(e); }}
      onKeyDown={e => { handleKey(e.key, e.currentTarget); onKeyDown(e); }}>
      {children}
    </DropdownItem>
  );
}


function handleKey(key, el) {
  if (key === "ArrowDown" || key === "ArrowRight") {
    if (el.nextElementSibling)
      // focus next
      el.nextElementSibling.focus();
    else
      // circle back to the first element
      el.parentNode.firstElementChild.focus();
  } else if (key === "ArrowUp" || key === "ArrowLeft") {
    if (el.previousElementSibling)
      // focus previous
      el.previousElementSibling.focus();
    else
      // circle back to last element at the end
      el.parentNode.lastElementChild.focus();
  }
}
