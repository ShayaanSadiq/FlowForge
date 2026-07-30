import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from '@mui/material';

const HTTP_PRESETS = [
  { label: 'GitHub — FlowForge repo', url: 'https://api.github.com/repos/ShayaanSadiq/FlowForge', method: 'GET' },
  { label: 'JSONPlaceholder — sample post', url: 'https://jsonplaceholder.typicode.com/posts/1', method: 'GET' },
  { label: 'Open-Meteo — Toronto weather', url: 'https://api.open-meteo.com/v1/forecast?latitude=43.65&longitude=-79.38&current_weather=true', method: 'GET' },
  { label: 'REST Countries — Canada', url: 'https://restcountries.com/v3.1/name/canada', method: 'GET' },
  { label: 'HTTPBin — POST echo', url: 'https://httpbin.org/post', method: 'POST', body: '{"message":"hello from FlowForge"}' },
];

const DATA_TRANSFORM_OPS = [
  { value: 'uppercase', label: 'Uppercase' },
  { value: 'lowercase', label: 'Lowercase' },
  { value: 'trim', label: 'Trim whitespace' },
  { value: 'reverse', label: 'Reverse text' },
  { value: 'slugify', label: 'Slugify (URL-friendly)' },
  { value: 'normalize_whitespace', label: 'Normalize whitespace' },
  { value: 'sort_lines', label: 'Sort lines' },
  { value: 'dedupe_lines', label: 'Dedupe lines' },
  { value: 'replace', label: 'Find & replace' },
  { value: 'extract_field', label: 'Extract JSON field' },
];

export const DEFAULT_FORM_STATE = {
  PYTHON_SCRIPT: {
    code: `print("Hello from FlowForge")

for i in range(3):
    print(f"step {i}")`,
  },
  HTTP_REQUEST: {
    preset: '',
    url: 'https://api.github.com/repos/ShayaanSadiq/FlowForge',
    method: 'GET',
    body: '',
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
  DATA_TRANSFORM: {
    operation: 'slugify',
    text: 'Hello FlowForge Jobs!',
    find: '',
    replaceWith: '',
    json: '{\n  "user": {\n    "name": "Alice",\n    "role": "admin"\n  }\n}',
    field: 'user.name',
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
    case 'HTTP_REQUEST': {
      const payload = { url: form.url.trim(), method: form.method };
      if (form.method === 'POST' && form.body.trim()) {
        payload.body = form.body;
      }
      return JSON.stringify(payload);
    }
    case 'JSON_FORMAT':
      return form.json.trim();
    case 'CSV_ANALYZE':
      return form.csv;
    case 'DATA_TRANSFORM': {
      const payload = { operation: form.operation };
      if (form.operation === 'extract_field') {
        payload.json = form.json;
        payload.field = form.field.trim();
      } else {
        payload.text = form.text;
        if (form.operation === 'replace') {
          payload.find = form.find;
          payload.replaceWith = form.replaceWith;
        }
      }
      return JSON.stringify(payload);
    }
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

    case 'HTTP_REQUEST':
      return (
        <>
          <FormControl fullWidth margin="normal">
            <InputLabel id="http-preset-label">Quick example (optional)</InputLabel>
            <Select
              labelId="http-preset-label"
              label="Quick example (optional)"
              value={form.preset}
              onChange={(e) => {
                const preset = HTTP_PRESETS.find((item) => item.label === e.target.value);
                if (preset) {
                  onChange({
                    preset: preset.label,
                    url: preset.url,
                    method: preset.method,
                    body: preset.body || '',
                  });
                } else {
                  setField('preset', '');
                }
              }}
            >
              <MenuItem value="">Custom request</MenuItem>
              {HTTP_PRESETS.map((preset) => (
                <MenuItem key={preset.label} value={preset.label}>{preset.label}</MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            fullWidth
            label="URL"
            value={form.url}
            onChange={(e) => setField('url', e.target.value)}
            margin="normal"
            required
            helperText="Public http/https URLs only. Private/local hosts are blocked."
            sx={monoFieldSx}
          />
          <FormControl fullWidth margin="normal">
            <InputLabel id="http-method-label">Method</InputLabel>
            <Select
              labelId="http-method-label"
              label="Method"
              value={form.method}
              onChange={(e) => setField('method', e.target.value)}
            >
              <MenuItem value="GET">GET</MenuItem>
              <MenuItem value="POST">POST</MenuItem>
              <MenuItem value="HEAD">HEAD</MenuItem>
            </Select>
          </FormControl>
          {form.method === 'POST' && (
            <TextField
              fullWidth
              label="Request body (optional)"
              value={form.body}
              onChange={(e) => setField('body', e.target.value)}
              margin="normal"
              multiline
              rows={6}
              helperText="Sent as JSON when non-empty."
              sx={monoFieldSx}
            />
          )}
        </>
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

    case 'DATA_TRANSFORM':
      return (
        <>
          <FormControl fullWidth margin="normal">
            <InputLabel id="transform-op-label">Operation</InputLabel>
            <Select
              labelId="transform-op-label"
              label="Operation"
              value={form.operation}
              onChange={(e) => setField('operation', e.target.value)}
            >
              {DATA_TRANSFORM_OPS.map((op) => (
                <MenuItem key={op.value} value={op.value}>{op.label}</MenuItem>
              ))}
            </Select>
          </FormControl>
          {form.operation === 'extract_field' ? (
            <>
              <TextField
                fullWidth
                label="JSON document"
                value={form.json}
                onChange={(e) => setField('json', e.target.value)}
                margin="normal"
                multiline
                rows={8}
                required
                sx={monoFieldSx}
              />
              <TextField
                fullWidth
                label="Field path"
                value={form.field}
                onChange={(e) => setField('field', e.target.value)}
                margin="normal"
                required
                helperText='Dot-separated path, e.g. user.name or features.0'
                sx={monoFieldSx}
              />
            </>
          ) : (
            <>
              <TextField
                fullWidth
                label="Text"
                value={form.text}
                onChange={(e) => setField('text', e.target.value)}
                margin="normal"
                multiline
                rows={6}
                required
                sx={monoFieldSx}
              />
              {form.operation === 'replace' && (
                <>
                  <TextField
                    fullWidth
                    label="Find"
                    value={form.find}
                    onChange={(e) => setField('find', e.target.value)}
                    margin="normal"
                    required
                    sx={monoFieldSx}
                  />
                  <TextField
                    fullWidth
                    label="Replace with"
                    value={form.replaceWith}
                    onChange={(e) => setField('replaceWith', e.target.value)}
                    margin="normal"
                    sx={monoFieldSx}
                  />
                </>
              )}
            </>
          )}
        </>
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
