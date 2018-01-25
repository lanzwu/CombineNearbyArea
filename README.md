# CombineNearbyArea
合并区域内的小矩形为更大的矩形

Combine combine = new Combine(Rect[] rects, int areaWidth, int areaHeight);
Combine combine = new Combine(Rect[] rects, int areaWidth, int areaHeight, boolean quickMode);
参数分别为：
rects：Rect[]类型的参数，代表区域内的不参与合并的矩形区域集合

areaHeight & areaWidth：区域的高度和宽度

quickMode：是否快速消耗未使用区域。参数默认为false，指定为true时，得到的合并后的区域会有重合部分，但是可使区域数量最少

