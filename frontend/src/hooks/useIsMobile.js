import { useMediaQuery, useTheme } from '@mui/material';

/**
 * Returns true when the viewport is below the given MUI breakpoint.
 */
export default function useIsMobile(breakpoint = 'md') {
  const theme = useTheme();
  return useMediaQuery(theme.breakpoints.down(breakpoint));
}
