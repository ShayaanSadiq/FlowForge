import { describe, expect, it } from 'vitest';
import { isSubmitShortcut } from './submitShortcut.js';

describe('isSubmitShortcut', () => {
  it('detects Ctrl+Enter', () => {
    expect(isSubmitShortcut({ key: 'Enter', ctrlKey: true, metaKey: false })).toBe(true);
  });

  it('detects Cmd+Enter on macOS', () => {
    expect(isSubmitShortcut({ key: 'Enter', ctrlKey: false, metaKey: true })).toBe(true);
  });

  it('ignores Enter without a modifier', () => {
    expect(isSubmitShortcut({ key: 'Enter', ctrlKey: false, metaKey: false })).toBe(false);
  });

  it('ignores other keys with modifiers', () => {
    expect(isSubmitShortcut({ key: 's', ctrlKey: true, metaKey: false })).toBe(false);
  });
});
