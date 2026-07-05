/**
 * 镜片信息「可输入 + 可选」选项工具
 *
 * 数据来源 = 前端预设常量 + localStorage 历史记忆。
 * 仅前端使用，不依赖后端接口。镜片品牌 / 镜片参数两个字段共用。
 */

/** 镜片品牌预设（主流品牌，按常见度排序） */
export const LENS_BRAND_PRESETS: string[] = [
  '明月',
  '铭远',
  '依视路',
  '蔡司',
  '豪雅',
  '万新',
  '凯米',
  '尼康',
  '康耐特',
];

/** 镜片参数预设（折射率 + 功能组合） */
export const LENS_PARAMS_PRESETS: string[] = [
  '1.56 单光',
  '1.56 防蓝光',
  '1.56 变色',
  '1.56 渐进',
  '1.60 单光',
  '1.60 防蓝光',
  '1.60 变色',
  '1.60 渐进',
  '1.67 单光',
  '1.67 防蓝光',
  '1.67 变色',
  '1.67 渐进',
  '1.74 单光',
  '1.74 防蓝光',
  '1.74 变色',
  '1.74 渐进'
];

/** localStorage 存储键名 */
const STORAGE_KEY = 'glasses_lens_history';

/** 每个字段历史记录上限 */
const MAX_HISTORY = 20;

type LensField = 'lensBrand' | 'lensParams';

interface LensHistory {
  lensBrand: string[];
  lensParams: string[];
}

/** 读取历史记录（容错：解析失败或隐私模式返回空结构） */
const readHistory = (): LensHistory => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return { lensBrand: [], lensParams: [] };
    const parsed = JSON.parse(raw);
    return {
      lensBrand: Array.isArray(parsed?.lensBrand) ? parsed.lensBrand : [],
      lensParams: Array.isArray(parsed?.lensParams) ? parsed.lensParams : []
    };
  } catch {
    return { lensBrand: [], lensParams: [] };
  }
};

/** 写入历史记录（容错：存储满或隐私模式静默失败） */
const writeHistory = (history: LensHistory): void => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(history));
  } catch {
    // 静默忽略：隐私模式 / 存储已满，不影响业务流程
  }
};

/**
 * 获取某字段的建议列表 = 预设 + 历史，去重，预设在前。
 */
export const getLensSuggestions = (field: LensField): string[] => {
  const presets = field === 'lensBrand' ? LENS_BRAND_PRESETS : LENS_PARAMS_PRESETS;
  const history = readHistory()[field];
  const merged: string[] = [];
  const seen = new Set<string>();
  for (const v of [...presets, ...history]) {
    const key = v?.trim();
    if (!key || seen.has(key)) continue;
    seen.add(key);
    merged.push(v);
  }
  return merged;
};

/**
 * 保存本次开单输入的镜片信息到历史记录。
 * 非空值插入头部，去重，截断保留前 MAX_HISTORY 条。
 */
export const saveLensHistory = (values: { lensBrand?: string; lensParams?: string }): void => {
  const history = readHistory();
  let changed = false;
  (['lensBrand', 'lensParams'] as LensField[]).forEach((field) => {
    const raw = values[field];
    const val = typeof raw === 'string' ? raw.trim() : '';
    if (!val) return;
    const list = history[field].filter((x) => x !== val);
    list.unshift(val);
    if (list.length > MAX_HISTORY) list.length = MAX_HISTORY;
    history[field] = list;
    changed = true;
  });
  if (changed) writeHistory(history);
};
