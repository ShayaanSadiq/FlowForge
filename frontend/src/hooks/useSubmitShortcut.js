import { useEffect } from 'react';
import { isSubmitShortcut } from '../utils/submitShortcut.js';

/**
 * Calls onSubmit when the user presses Ctrl+Enter or Cmd+Enter.
 */
export function useSubmitShortcut(onSubmit, { enabled = true } = {}) {
  useEffect(() => {
    if (!enabled) {
      return undefined;
    }

    const handleKeyDown = (event) => {
      if (!isSubmitShortcut(event)) {
        return;
      }

      event.preventDefault();
      onSubmit(event);
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [enabled, onSubmit]);
}
