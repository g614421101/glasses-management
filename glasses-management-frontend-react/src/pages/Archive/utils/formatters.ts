export const fmt = (val: any): string => {
  if (val === undefined || val === null || val === '') return '-';
  const num = parseFloat(String(val));
  if (isNaN(num)) return '-';
  return num > 0 ? '+' + num.toFixed(2) : num.toFixed(2);
};

export const fmtInput = (val: any): string => {
  if (val === undefined || val === null || val === '') return '';
  const num = parseFloat(String(val));
  if (isNaN(num)) return '';
  return num > 0 ? '+' + num.toFixed(2) : num.toFixed(2);
};
