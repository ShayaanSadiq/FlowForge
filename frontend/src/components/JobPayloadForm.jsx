import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from '@mui/material';

const HASH_LIMITS = {
  singleChars: 100_000,
  linesModeChars: 200_000,
  maxLines: 500,
  maxLineChars: 10_000,
};

export const DEFAULT_FORM_STATE = {
  PYTHON_SCRIPT: {
    code: `print("Hello from FlowForge")

for i in range(3):
    print(f"step {i}")`,
  },
  JSON_FORMAT: {
    json: `{
  "name": "FlowForge",
  "features": ["jobs", "worker", "mongodb"]
}`,
  },
  CSV_ANALYZE: {
    csv: `name,email,score
Alice,alice@example.com,92
Bob,bob@example.com,
Carol,carol@example.com,88`,
  },
  HASH_GENERATE: {
    text: 'password-to-hash',
    algorithm: 'SHA-256',
    mode: 'single',
    expected: '',
  },
  BASE64_CODEC: {
    operation: 'encode',
    text: 'Hello FlowForge',
  },
};

export function getDefaultFormState(type) {
  return structuredClone(DEFAULT_FORM_STATE[type] || {});
}

export function buildPayload(type, form) {
  switch (type) {
    case 'PYTHON_SCRIPT':
      return form.code;
    case 'JSON_FORMAT':
      return form.json.trim();
    case 'CSV_ANALYZE':
      return form.csv;
    case 'HASH_GENERATE': {
      const payload = {
        text: form.text,
        algorithm: form.algorithm,
        mode: form.mode,
      };
      if (form.mode === 'single' && form.expected.trim()) {
        payload.expected = form.expected.trim();
      }
      return JSON.stringify(payload);
    }
    case 'BASE64_CODEC':
      return JSON.stringify({
        operation: form.operation,
        text: form.text,
      });
    default:
      throw new Error(`Unsupported job type: ${type}`);
  }
}

const monoFieldSx = { '& textarea, & input': { fontFamily: 'monospace', fontSize: 13 } };

const hashHelperText = `Single string: up to ${HASH_LIMITS.singleChars.toLocaleString()} chars. `
  + `Line mode: up to ${HASH_LIMITS.maxLines} lines, ${HASH_LIMITS.maxLineChars.toLocaleString()} chars per line, `
  + `${HASH_LIMITS.linesModeChars.toLocaleString()} chars total.`;

export default function JobPayloadForm({ type, form, onChange }) {
  const setField = (field, value) => onChange({ ...form, [field]: value });

  switch (type) {
    case 'PYTHON_SCRIPT':
      return (
        <TextField
          fullWidth
          label="Python code"
          value={form.code}
          onChange={(e) => setField('code', e.target.value)}
          margin="normal"
          multiline
          rows={12}
          required
          helperText="Python 3 code. stdout becomes the job result. Timeout: 30s."
          sx={monoFieldSx}
        />
      );

    case 'JSON_FORMAT':
      return (
        <TextField
          fullWidth
          label="JSON to validate"
          value={form.json}
          onChange={(e) => setField('json', e.target.value)}
          margin="normal"
          multiline
          rows={10}
          required
          helperText="Paste raw JSON. The job validates and pretty-prints it."
          sx={monoFieldSx}
        />
      );

    case 'CSV_ANALYZE':
      return (
        <TextField
          fullWidth
          label="CSV data"
          value={form.csv}
          onChange={(e) => setField('csv', e.target.value)}
          margin="normal"
          multiline
          rows={10}
          required
          helperText="Paste CSV text. Returns row/column stats and fill rates."
          sx={monoFieldSx}
        />
      );

    case 'HASH_GENERATE':
      return (
        <>
          <TextField
            fullWidth
            label="Text to hash"
            value={form.text}
            onChange={(e) => setField('text', e.target.value)}
            margin="normal"
            multiline
            rows={form.mode === 'lines' ? 8 : 3}
            required
            helperText={hashHelperText}
            inputProps={{
              maxLength: form.mode === 'lines' ? HASH_LIMITS.linesModeChars : HASH_LIMITS.singleChars,
            }}
            sx={monoFieldSx}
          />
          <FormControl fullWidth margin="normal">
            <InputLabel id="hash-algo-label">Algorithm</InputLabel>
            <Select
              labelId="hash-algo-label"
              label="Algorithm"
              value={form.algorithm}
              onChange={(e) => setField('algorithm', e.target.value)}
            >
              <MenuItem value="SHA-256">SHA-256</MenuItem>
              <MenuItem value="SHA-512">SHA-512</MenuItem>
            </Select>
          </FormControl>
          <FormControl fullWidth margin="normal">
            <InputLabel id="hash-mode-label">Mode</InputLabel>
            <Select
              labelId="hash-mode-label"
              label="Mode"
              value={form.mode}
              onChange={(e) => setField('mode', e.target.value)}
            >
              <MenuItem value="single">Single string</MenuItem>
              <MenuItem value="lines">One hash per line</MenuItem>
            </Select>
          </FormControl>
          {form.mode === 'single' && (
            <TextField
              fullWidth
              label="Expected hash (optional)"
              value={form.expected}
              onChange={(e) => setField('expected', e.target.value)}
              margin="normal"
              helperText="If provided, the job verifies the computed hash."
              sx={monoFieldSx}
            />
          )}
        </>
      );

    case 'BASE64_CODEC':
      return (
        <>
          <FormControl fullWidth margin="normal">
            <InputLabel id="b64-op-label">Operation</InputLabel>
            <Select
              labelId="b64-op-label"
              label="Operation"
              value={form.operation}
              onChange={(e) => setField('operation', e.target.value)}
            >
              <MenuItem value="encode">Encode</MenuItem>
              <MenuItem value="decode">Decode</MenuItem>
            </Select>
          </FormControl>
          <TextField
            fullWidth
            label={form.operation === 'decode' ? 'Base64 input' : 'Text to encode'}
            value={form.text}
            onChange={(e) => setField('text', e.target.value)}
            margin="normal"
            multiline
            rows={4}
            required
            sx={monoFieldSx}
          />
        </>
      );

    default:
      return null;
  }
}
