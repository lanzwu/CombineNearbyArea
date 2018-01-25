# CombineNearbyArea
合并区域内的小矩形为更大的矩形

Combine combine = new Combine(Rect[] rects, int areaWidth, int areaHeight);
Combine combine = new Combine(Rect[] rects, int areaWidth, int areaHeight, boolean quickMode);

参数分别为：

rects：Rect[]类型的参数，代表区域内的不参与合并的矩形区域集合

areaHeight & areaWidth：区域的高度和宽度

quickMode：是否快速消耗未使用区域。参数默认为false，指定为true时，得到的合并后的区域会有重合部分，但是可使区域数量最少

quickmode = false,合并后的区域没有重合部分，一般情况下区域总数大于quickmode = true时的情况
![quickmode = false](https://github.com/lanzwu/CombineNearbyArea/blob/lanzwu/fastmode-false.png)

quickmode = true，区域之间可以相互重合，但是合并后区域总数较少
![quickmode = true](https://github.com/lanzwu/CombineNearbyArea/blob/lanzwu/fastmode-true.png)

方法：
public ArrayList<Rect> getResult();
获取合并后的Rect类型区域列表

