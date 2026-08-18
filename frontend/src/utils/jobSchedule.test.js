import { describe, expect, it, vi } from 'vitest';
import {
  buildScheduleFields,
  describeScheduleSummary,
  formatScheduledAt,
  formatScheduleDuration,
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

describe('formatScheduleDuration', () => {
  it('formats short delays in seconds', () => {
    expect(formatScheduleDuration(30)).toBe('30 seconds');
  });

  it('formats longer delays with multiple units', () => {
    expect(formatScheduleDuration(3_660)).toBe('1 hour 1 minute');
    expect(formatScheduleDuration(86_400)).toBe('1 day');
  });

  it('treats zero as immediate wording', () => {
    expect(formatScheduleDuration(0)).toBe('immediately');
  });
});

describe('describeScheduleSummary', () => {
  const now = new Date('2026-07-30T12:00:00Z');

  it('describes immediate runs', () => {
    const summary = describeScheduleSummary({ mode: 'immediate' }, now);
    expect(summary.title).toBe('Run immediately');
    expect(summary.valid).toBe(true);
  });

  it('describes delayed runs with a computed run time', () => {
    const summary = describeScheduleSummary(
      { mode: 'delay', delaySeconds: 120 },
      now,
    );

    expect(summary.title).toBe('Runs in 2 minutes');
    expect(summary.detail).toContain('Scheduled for');
    expect(summary.valid).toBe(true);
  });

  it('flags invalid delays', () => {
    const summary = describeScheduleSummary(
      { mode: 'delay', delaySeconds: -5 },
      now,
    );

    expect(summary.valid).toBe(false);
    expect(summary.severity).toBe('error');
  });

  it('describes datetime runs', () => {
    vi.useFakeTimers();
    vi.setSystemTime(now);

    const summary = describeScheduleSummary({
      mode: 'datetime',
      scheduledAtLocal: '2026-07-30T14:30',
    });

    expect(summary.title).toContain('Runs at');
    expect(summary.detail).toContain('from now');
    expect(summary.valid).toBe(true);
    vi.useRealTimers();
  });

  it('flags past datetime selections', () => {
    vi.useFakeTimers();
    vi.setSystemTime(now);

    const summary = describeScheduleSummary({
      mode: 'datetime',
      scheduledAtLocal: '2026-07-30T10:00',
    });

    expect(summary.valid).toBe(false);
    expect(summary.title).toBe('Run time is in the past');
    vi.useRealTimers();
  });
});
