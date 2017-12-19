# Transdoc概述

Transdoc的目标是快速地将word文档转换为格式一致的Markdown文件。支持Java 6及以上版本

Markdown是一门专注于文档结构的语言工具，可十分方便地转换为html或者在网页中展示。本工具的目标是生成尽可能符合规范的Markdown文本。

当编写的word文档中包含复杂的文本格式或字体颜色时，工具会忽略这些样式。因为markdown中在网页上显示时，会打乱页面已经定义好的样式效果。因此，编写word文档的时候，如果只是为了用来转换成markdown，则不需要花费过多的精力来添加复杂的文本格式（包括字体颜色，字间距，段间距等等）。

在特殊情况下(比如，表格单元格内容中的段落换行)，工具会使用html标签“&lt;br&gt;“来代替换行符号“\n“。

Markdown支持两种标题的编写格式，分别是Setext和atx，本工具使用atx格式生成标题，优点在于可以支持与html同样数量的标题级别(6级)。atx的标题格式如下：  

	# 一级标题  
	## 二级标题  
	### 三级标题  
	...

## Word编写格式说明

### 注意问题：

1. 目前支持图片/表格/段落的单独识别和提取，但表格中嵌套的图片等暂时无法解析；
2. 文字样式只能单独显示粗体或斜体其中一种，如字体样式同时为粗斜体，则优先显示粗体；
3. 在带序号的列表段落下方的文本内容，可能由于段落的缩进造成列表合并的现象，需要手动调整缩进分割列表块；
4. 在转换完成后，可能会有表格无法显示的问题，需要检查下每行行首是否有缩进，把所有缩进去除即可。
5. 文字颜色，段间距，字间距等格式内容不需要进行过多设置，工具转换时不会保留这些样式。除非word文档除了转换成Markdown以外还有其他用途。

### 要求:

- 标题(**重要**)：
    1. **文档的标题使用大纲级别1级**；
    2. 章节和子标题须使用正确的大纲级别定义(2级和以上大纲级别)，并**去除列表自动编号**。

- 列表：
    1. 列表序号后**加一个空格**；
    2. 避免使用列表层级嵌套，如有多级列表，优先考虑采用低级标题；
    3. 列表可用两种方式：  
        1) 设置列表符号或列表编号;  
        2) 不设置项目符号和编号，按markdown格式手动添加列表符号，如:[+、-、*、1.] 等等；

- 表格：
    1. 插入脚本及代码时，需要使用1x1的表格，本工具将识别为代码块；
    2. 如有连续的独立代码块，应将其合并为一个；
    2. 由于markdown缺少支持，尽量不要使用合并单元格，本工具可以兼容合并单元格但不能保证最终效果；

- 正文：
    1. 插入url链接在**需独立一行**，后面带一个空格。
    2. 强调文字使用粗体样式，切勿使用字体颜色等(注：markdown是注重于文档结构的语言，展示样式具有一定复杂度，且打乱md的语法美感)

### 建议:
1. 尽量利用上6级标题，减少嵌套列表的使用。嵌套的列表层级不便于识别缩进和展示。

## 软件使用说明

下载获取分发包transdoc-bin.zip，解压到任意目录，点击run.bat即可执行程序，程序将扫描./docs目录下的doc文件并进行转换，输出到./docs目录中。  
或者解压后打开how_to_use.txt阅读使用说明。

## 意见反馈

使用过程中如果遇到任何问题，欢迎到https://github.com/verils/transdoc/issues 提Issue讨论。