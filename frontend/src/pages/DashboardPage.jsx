import { useCallback, useEffect, useRef, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Pagination,
  Paper,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { jobsApi, statsApi } from '../api/client';
import StatusBadge from '../components/StatusBadge';
import { formatScheduledAt } from '../components/JobScheduleForm';
import { useToast } from '../context/ToastContext';

const PAGE_SIZE_OPTIONS = [20, 50, 100];

const STATUS_FILTERS = [
  { value: 'ALL', label: 'All statuses' },
  { value: 'SCHEDULED', label: 'Scheduled' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'RUNNING', label: 'Running' },
  { value: 'SUCCEEDED', label: 'Succeeded' },
  { value: 'FAILED', label: 'Failed' },
  { value: 'DEAD_LETTER', label: 'Dead letter' },
];

const TYPE_FILTERS = [
  { value: 'ALL', label: 'All types' },
  { value: 'PYTHON_SCRIPT', label: 'Python Script' },
  { value: 'JSON_FORMAT', label: 'JSON Format' },
  { value: 'CSV_ANALYZE', label: 'CSV Analyze' },
  { value: 'HASH_GENERATE', label: 'Hash Generate' },
  { value: 'BASE64_CODEC', label: 'Base64 Codec' },
];

const SORT_OPTIONS = [
  { value: 'createdAt,desc', label: 'Newest first' },
  { value: 'createdAt,asc', label: 'Oldest first' },
];

export default function DashboardPage() {
  const [jobs, setJobs] = useState([]);
  const [stats, setStats] = useState(null);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [sort, setSort] = useState('createdAt,desc');
  const [totalJobs, setTotalJobs] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const { showToast } = useToast();
  const lastErrorRef = useRef('');

  const loadData = useCallback(async () => {
    try {
      const [jobsPage, statsData] = await Promise.all([
        jobsApi.list({
          page,
          size: pageSize,
          status: statusFilter,
          type: typeFilter,
          sort,
        }),
        statsApi.get(),
      ]);

      const pages = jobsPage.totalPages ?? 0;
      if (pages > 0 && page >= pages) {
        setPage(pages - 1);
        return;
      }

      setJobs(jobsPage.content || []);
      setTotalJobs(jobsPage.totalElements ?? 0);
      setTotalPages(pages);
      setStats(statsData);
      lastErrorRef.current = '';
    } catch (err) {
      if (err.message !== lastErrorRef.current) {
        lastErrorRef.current = err.message;
        showToast(err.message, 'error');
      }
    }
  }, [page, pageSize, statusFilter, typeFilter, sort, showToast]);

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 5000);
    return () => clearInterval(interval);
  }, [loadData]);

  const handlePageSizeChange = (event) => {
    setPageSize(event.target.value);
    setPage(0);
  };

  const handlePageChange = (_event, value) => {
    setPage(value - 1);
  };

  const handleStatusFilterChange = (event) => {
    setStatusFilter(event.target.value);
    setPage(0);
  };

  const handleTypeFilterChange = (event) => {
    setTypeFilter(event.target.value);
    setPage(0);
  };

  const handleSortChange = (event) => {
    setSort(event.target.value);
    setPage(0);
  };

  const hasActiveFilters =
    statusFilter !== 'ALL' || typeFilter !== 'ALL' || sort !== 'createdAt,desc';

  const handleClearFilters = () => {
    setStatusFilter('ALL');
    setTypeFilter('ALL');
    setSort('createdAt,desc');
    setPage(0);
  };

  const rangeStart = totalJobs === 0 ? 0 : page * pageSize + 1;
  const rangeEnd = Math.min((page + 1) * pageSize, totalJobs);

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" fontWeight={700}>Job Dashboard</Typography>
        <Button component={RouterLink} to="/jobs/new" variant="contained">Submit Job</Button>
      </Box>

      <Grid container spacing={2} sx={{ mb: 3 }}>
        {stats && Object.entries(stats).map(([key, value]) => (
          <Grid item xs={6} sm={4} md={2.4} key={key}>
            <Card>
              <CardContent>
                <Typography variant="overline" color="text.secondary">{key}</Typography>
                <Typography variant="h5" fontWeight={700}>{value}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Paper sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth size="small">
              <InputLabel id="status-filter-label">Status</InputLabel>
              <Select
                labelId="status-filter-label"
                value={statusFilter}
                label="Status"
                onChange={handleStatusFilterChange}
              >
                {STATUS_FILTERS.map((option) => (
                  <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth size="small">
              <InputLabel id="type-filter-label">Type</InputLabel>
              <Select
                labelId="type-filter-label"
                value={typeFilter}
                label="Type"
                onChange={handleTypeFilterChange}
              >
                {TYPE_FILTERS.map((option) => (
                  <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth size="small">
              <InputLabel id="sort-filter-label">Sort by created</InputLabel>
              <Select
                labelId="sort-filter-label"
                value={sort}
                label="Sort by created"
                onChange={handleSortChange}
              >
                {SORT_OPTIONS.map((option) => (
                  <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
        </Grid>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
          <Button
            size="small"
            variant="outlined"
            onClick={handleClearFilters}
            disabled={!hasActiveFilters}
          >
            Clear filters
          </Button>
        </Box>
      </Paper>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, flexWrap: 'wrap', gap: 2 }}>
        <Typography variant="body2" color="text.secondary">
          {totalJobs === 0
            ? 'No jobs match the current filters'
            : `Showing ${rangeStart}–${rangeEnd} of ${totalJobs} job${totalJobs !== 1 ? 's' : ''}`}
        </Typography>
        <FormControl size="small" sx={{ minWidth: 140 }}>
          <InputLabel id="page-size-label">Jobs per page</InputLabel>
          <Select
            labelId="page-size-label"
            value={pageSize}
            label="Jobs per page"
            onChange={handlePageSizeChange}
          >
            {PAGE_SIZE_OPTIONS.map((size) => (
              <MenuItem key={size} value={size}>{size}</MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Runs at</TableCell>
              <TableCell>Attempts</TableCell>
              <TableCell>Created</TableCell>
              <TableCell />
            </TableRow>
          </TableHead>
          <TableBody>
            {jobs.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">No jobs match the current filters.</TableCell>
              </TableRow>
            ) : jobs.map((job) => (
              <TableRow key={job.id} hover>
                <TableCell sx={{ fontFamily: 'monospace', fontSize: 12 }}>{job.id?.slice(-8)}</TableCell>
                <TableCell>{job.type}</TableCell>
                <TableCell><StatusBadge status={job.status} scheduledAt={job.scheduledAt} /></TableCell>
                <TableCell>{formatScheduledAt(job.scheduledAt)}</TableCell>
                <TableCell>{job.attempts}/{job.maxAttempts}</TableCell>
                <TableCell>{job.createdAt ? new Date(job.createdAt).toLocaleString() : '-'}</TableCell>
                <TableCell>
                  <Button component={RouterLink} to={`/jobs/${job.id}`} size="small">View</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {totalPages > 1 && (
          <Stack direction="row" justifyContent="center" sx={{ py: 2 }}>
            <Pagination
              count={totalPages}
              page={page + 1}
              onChange={handlePageChange}
              color="primary"
              showFirstButton
              showLastButton
            />
          </Stack>
        )}
      </Paper>
    </Box>
  );
}
