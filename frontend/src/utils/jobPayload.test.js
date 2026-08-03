import { describe, expect, it } from 'vitest';
import { buildPayload, parsePayloadToForm } from '../components/JobPayloadForm.jsx';

describe('parsePayloadToForm', () => {
  it('maps python script payload to form code field', () => {
    expect(parsePayloadToForm('PYTHON_SCRIPT', 'print("hi")')).toEqual({
      code: 'print("hi")',
    });
  });

  it('maps hash json payload back to form fields', () => {
    const form = parsePayloadToForm(
      'HASH_GENERATE',
      '{"text":"secret","algorithm":"SHA-512","mode":"lines"}',
    );

    expect(form.text).toBe('secret');
    expect(form.algorithm).toBe('SHA-512');
    expect(form.mode).toBe('lines');
  });

  it('supports plain-text hash payloads', () => {
    expect(parsePayloadToForm('HASH_GENERATE', 'secret').text).toBe('secret');
  });
});

describe('buildPayload', () => {
  it('round-trips python script payloads', () => {
    const form = { code: 'print("hello")' };
    const payload = buildPayload('PYTHON_SCRIPT', form);

    expect(parsePayloadToForm('PYTHON_SCRIPT', payload)).toEqual(form);
  });

  it('builds hash payload with optional expected hash', () => {
    const payload = buildPayload('HASH_GENERATE', {
      text: 'secret',
      algorithm: 'SHA-256',
      mode: 'single',
      expected: 'abc123',
    });

    expect(JSON.parse(payload)).toEqual({
      text: 'secret',
      algorithm: 'SHA-256',
      mode: 'single',
      expected: 'abc123',
    });
  });

  it('omits expected hash when blank', () => {
    const payload = buildPayload('HASH_GENERATE', {
      text: 'secret',
      algorithm: 'SHA-256',
      mode: 'single',
      expected: '   ',
    });

    expect(JSON.parse(payload)).toEqual({
      text: 'secret',
      algorithm: 'SHA-256',
      mode: 'single',
    });
  });
});
