export const formatMoney = (amount: number | undefined | null): string => {
  if (amount === undefined || amount === null) return '0.00';
  return amount.toFixed(2);
};

export const formatDiscount = (actual: number, retail: number): string => {
  if (retail <= 0) return '-';
  const discount = (actual / retail) * 10;
  return discount.toFixed(1) + '折';
};

export const formatSphValue = (val: number | string | undefined | null): string => {
  if (val === undefined || val === null || val === '') return '-';
  const num = typeof val === 'string' ? parseFloat(val) : val;
  if (isNaN(num)) return '-';
  return num > 0 ? '+' + num.toFixed(2) : num.toFixed(2);
};
