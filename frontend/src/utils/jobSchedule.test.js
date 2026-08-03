import { describe, expect, it, vi } from 'vitest';
import {
  buildScheduleFields,
  formatScheduledAt,
  isJobScheduled,
} from '../components/JobScheduleForm.jsx';

describe('buildScheduleFields', () => {
  it('returns empty object for immediate runs', () => {
    expect(buildScheduleFields({ mode: 'immediate' })).toEqual({});
  });

  it('returns delaySeconds for delayed runs', () => {
    expect(buildScheduleFields({ mode: 'delay', delaySeconds: 45 })).toEqual({
      delaySeconds: 45,
    });
  });

  it('rejects negative delays', () => {
    expect(() => buildScheduleFields({ mode: 'delay', delaySeconds: -1 }))
      .toThrow(/non-negative/i);
  });

  it('returns scheduledAt iso string for datetime runs', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-07-30T12:00:00Z'));

    const result = buildScheduleFields({
      mode: 'datetime',
      scheduledAtLocal: '2026-07-30T14:30',
    });

    expect(result.scheduledAt).toMatch(/2026-07-30/);
    vi.useRealTimers();
  });
});

describe('isJobScheduled', () => {
  it('detects future pending jobs as scheduled', () => {
    const future = new Date(Date.now() + 60_000).toISOString();
    expect(isJobScheduled('PENDING', future)).toBe(true);
  });

  it('treats past pending jobs as ready', () => {
    const past = new Date(Date.now() - 60_000).toISOString();
    expect(isJobScheduled('PENDING', past)).toBe(false);
  });
});

describe('formatScheduledAt', () => {
  it('returns dash for missing values', () => {
    expect(formatScheduledAt(null)).toBe('-');
  });
});
