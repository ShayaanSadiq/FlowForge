/**
 * Normalizes Spring Data VIA_DTO paginated responses into a flat shape
 * the dashboard already understands.
 */
export function normalizePageResponse(page) {
  if (!page || !page.page) {
    return page;
  }

  return {
    content: page.content,
    totalElements: page.page.totalElements,
    totalPages: page.page.totalPages,
    number: page.page.number,
    size: page.page.size,
  };
}

/**
 * Maps RFC 7807 ProblemDetail and similar API error bodies to user-facing text.
 */
export function parseApiError(body, statusText = 'Request failed') {
  if (!body) {
    return statusText;
  }

  if (typeof body === 'string') {
    return body;
  }

  if (typeof body.detail === 'string') {
    return body.detail;
  }

  if (Array.isArray(body.detail)) {
    return body.detail
      .map((item) => (typeof item === 'string' ? item : item.msg))
      .filter(Boolean)
      .join(', ');
  }

  if (typeof body.message === 'string') {
    return body.message;
  }

  if (body.title && body.status) {
    return `${body.title} (${body.status})`;
  }

  return statusText;
}
