import { describe, expect, it } from 'vitest';
import { normalizePageResponse, parseApiError } from './apiUtils.js';

describe('normalizePageResponse', () => {
  it('flattens Spring Data page metadata', () => {
    const normalized = normalizePageResponse({
      content: [{ id: '1' }],
      page: {
        totalElements: 1,
        totalPages: 1,
        number: 0,
        size: 20,
      },
    });

    expect(normalized).toEqual({
      content: [{ id: '1' }],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 20,
    });
  });

  it('passes through legacy flat responses', () => {
    const page = { content: [], totalElements: 0, totalPages: 0 };
    expect(normalizePageResponse(page)).toBe(page);
  });
});

describe('parseApiError', () => {
  it('reads ProblemDetail detail string', () => {
    expect(parseApiError({ detail: 'Job not found' }, 'Bad Request')).toBe('Job not found');
  });

  it('joins validation error arrays', () => {
    expect(parseApiError({
      detail: [{ msg: 'type must not be null' }, { msg: 'payload must not be blank' }],
    })).toBe('type must not be null, payload must not be blank');
  });

  it('falls back to status text', () => {
    expect(parseApiError(null, 'Unauthorized')).toBe('Unauthorized');
  });
});
