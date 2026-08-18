/**
 * Returns true when the user pressed Ctrl+Enter or Cmd+Enter.
 */
export function isSubmitShortcut(event) {
  return event.key === 'Enter' && (event.ctrlKey || event.metaKey);
}
