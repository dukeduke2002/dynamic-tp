import{_ as n}from"./plugin-vue_export-helper.21dcd24c.js";import{o as s,c as a,d as e}from"./app.5e87ad87.js";const o={},p=e(`<p><strong>\u4F7F\u7528 ThreadPoolExecutor \u8FC7\u7A0B\u4E2D\u4F60\u662F\u5426\u6709\u4EE5\u4E0B\u75DB\u70B9\u5462\uFF1F</strong></p><blockquote><p>1.\u4EE3\u7801\u4E2D\u521B\u5EFA\u4E86\u4E00\u4E2A ThreadPoolExecutor\uFF0C\u4F46\u662F\u4E0D\u77E5\u9053\u90A3\u51E0\u4E2A\u6838\u5FC3\u53C2\u6570\u8BBE\u7F6E\u591A\u5C11\u6BD4\u8F83\u5408\u9002</p><p>2.\u51ED\u7ECF\u9A8C\u8BBE\u7F6E\u53C2\u6570\u503C\uFF0C\u4E0A\u7EBF\u540E\u53D1\u73B0\u9700\u8981\u8C03\u6574\uFF0C\u6539\u4EE3\u7801\u91CD\u542F\u670D\u52A1\uFF0C\u975E\u5E38\u9EBB\u70E6</p><p>3.\u7EBF\u7A0B\u6C60\u76F8\u5BF9\u5F00\u53D1\u4EBA\u5458\u6765\u8BF4\u662F\u4E2A\u9ED1\u76D2\uFF0C\u8FD0\u884C\u60C5\u51B5\u4E0D\u80FD\u611F\u77E5\u5230\uFF0C\u76F4\u5230\u51FA\u73B0\u95EE\u9898</p></blockquote><p>\u5982\u679C\u4F60\u6709\u4EE5\u4E0A\u75DB\u70B9\uFF0C\u52A8\u6001\u53EF\u76D1\u63A7\u7EBF\u7A0B\u6C60\uFF08DynamicTp\uFF09\u6216\u8BB8\u80FD\u5E2E\u52A9\u5230\u4F60\u3002</p><p>\u5982\u679C\u770B\u8FC7 ThreadPoolExecutor \u7684\u6E90\u7801\uFF0C\u5927\u6982\u53EF\u4EE5\u77E5\u9053\u5176\u5B9E\u5B83\u6709\u63D0\u4F9B\u4E00\u4E9B set \u65B9\u6CD5\uFF0C\u53EF\u4EE5\u5728\u8FD0\u884C\u65F6\u52A8\u6001\u53BB\u4FEE\u6539\u76F8\u5E94\u7684\u503C\uFF0C\u8FD9\u4E9B\u65B9\u6CD5\u6709\uFF1A</p><div class="language-java ext-java line-numbers-mode"><pre class="language-java"><code><span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">setCorePoolSize</span><span class="token punctuation">(</span><span class="token keyword">int</span> corePoolSize<span class="token punctuation">)</span><span class="token punctuation">;</span>
<span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">setMaximumPoolSize</span><span class="token punctuation">(</span><span class="token keyword">int</span> maximumPoolSize<span class="token punctuation">)</span><span class="token punctuation">;</span>
<span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">setKeepAliveTime</span><span class="token punctuation">(</span><span class="token keyword">long</span> time<span class="token punctuation">,</span> <span class="token class-name">TimeUnit</span> unit<span class="token punctuation">)</span><span class="token punctuation">;</span>
<span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">setThreadFactory</span><span class="token punctuation">(</span><span class="token class-name">ThreadFactory</span> threadFactory<span class="token punctuation">)</span><span class="token punctuation">;</span>
<span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">setRejectedExecutionHandler</span><span class="token punctuation">(</span><span class="token class-name">RejectedExecutionHandler</span> handler<span class="token punctuation">)</span><span class="token punctuation">;</span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p>\u73B0\u5728\u5927\u591A\u6570\u7684\u4E92\u8054\u7F51\u9879\u76EE\u5176\u5B9E\u90FD\u4F1A\u5FAE\u670D\u52A1\u5316\u90E8\u7F72\uFF0C\u6709\u4E00\u5957\u81EA\u5DF1\u7684\u670D\u52A1\u6CBB\u7406\u4F53\u7CFB\uFF0C\u5FAE\u670D\u52A1\u7EC4\u4EF6\u4E2D\u7684\u5206\u5E03\u5F0F\u914D\u7F6E\u4E2D\u5FC3\u626E\u6F14\u7684\u5C31\u662F\u52A8\u6001\u4FEE\u6539\u914D\u7F6E\uFF0C \u5B9E\u65F6\u751F\u6548\u7684\u89D2\u8272\u3002\u90A3\u4E48\u6211\u4EEC\u662F\u5426\u53EF\u4EE5\u7ED3\u5408\u914D\u7F6E\u4E2D\u5FC3\u6765\u505A\u8FD0\u884C\u65F6\u7EBF\u7A0B\u6C60\u53C2\u6570\u7684\u52A8\u6001\u8C03\u6574\u5462\uFF1F\u7B54\u6848\u662F\u80AF\u5B9A\u7684\uFF0C\u800C\u4E14\u914D\u7F6E\u4E2D\u5FC3\u76F8\u5BF9\u90FD\u662F\u9AD8\u53EF\u7528\u7684\uFF0C \u4F7F\u7528\u5B83\u4E5F\u4E0D\u7528\u8FC7\u4E8E\u62C5\u5FC3\u914D\u7F6E\u63A8\u9001\u51FA\u73B0\u95EE\u9898\u8FD9\u7C7B\u4E8B\u513F\uFF0C\u800C\u4E14\u4E5F\u80FD\u51CF\u5C11\u7814\u53D1\u52A8\u6001\u7EBF\u7A0B\u6C60\u7EC4\u4EF6\u672C\u8EAB\u7684\u96BE\u5EA6\u548C\u5DE5\u4F5C\u91CF\u3002</p><p><strong>\u7EFC\u4E0A\uFF0C\u53EF\u4EE5\u603B\u7ED3\u51FA\u4EE5\u4E0B\u7684\u80CC\u666F</strong></p><ul><li><p>\u5E7F\u6CDB\u6027\uFF1A\u5728 Java \u5F00\u53D1\u4E2D\uFF0C\u60F3\u8981\u63D0\u9AD8\u7CFB\u7EDF\u6027\u80FD\uFF0C\u7EBF\u7A0B\u6C60\u5DF2\u7ECF\u662F\u4E00\u4E2A 90%\u4EE5\u4E0A\u7684\u4EBA\u90FD\u4F1A\u9009\u62E9\u4F7F\u7528\u7684\u57FA\u7840\u5DE5\u5177</p></li><li><p>\u4E0D\u786E\u5B9A\u6027\uFF1A\u9879\u76EE\u4E2D\u53EF\u80FD\u4F1A\u521B\u5EFA\u5F88\u591A\u7EBF\u7A0B\u6C60\uFF0C\u65E2\u6709 IO \u5BC6\u96C6\u578B\u7684\uFF0C\u4E5F\u6709 CPU \u5BC6\u96C6\u578B\u7684\uFF0C\u4F46\u7EBF\u7A0B\u6C60\u7684\u53C2\u6570\u5E76\u4E0D\u597D\u786E\u5B9A\uFF1B\u9700\u8981\u6709\u5957\u673A\u5236\u5728\u8FD0\u884C\u8FC7\u7A0B\u4E2D\u52A8\u6001\u53BB\u8C03\u6574\u53C2\u6570</p></li><li><p>\u65E0\u611F\u77E5\u6027\uFF0C\u7EBF\u7A0B\u6C60\u8FD0\u884C\u8FC7\u7A0B\u4E2D\u7684\u5404\u9879\u6307\u6807\u4E00\u822C\u611F\u77E5\u4E0D\u5230\uFF1B\u9700\u8981\u6709\u5957\u76D1\u63A7\u62A5\u8B66\u673A\u5236\u5728\u4E8B\u524D\u3001\u4E8B\u4E2D\u5C31\u80FD\u8BA9\u5F00\u53D1\u4EBA\u5458\u611F\u77E5\u5230\u7EBF\u7A0B\u6C60\u7684\u8FD0\u884C\u72B6\u51B5\uFF0C\u53CA\u65F6\u5904\u7406</p></li><li><p>\u9AD8\u53EF\u7528\u6027\uFF0C\u914D\u7F6E\u53D8\u66F4\u9700\u8981\u53CA\u65F6\u63A8\u9001\u5230\u5BA2\u6237\u7AEF\uFF1B\u9700\u8981\u6709\u9AD8\u53EF\u7528\u7684\u914D\u7F6E\u7BA1\u7406\u63A8\u9001\u670D\u52A1\uFF0C\u914D\u7F6E\u4E2D\u5FC3\u662F\u73B0\u5728\u5927\u591A\u6570\u4E92\u8054\u7F51\u7CFB\u7EDF\u90FD\u4F1A\u4F7F\u7528\u7684\u7EC4\u4EF6\uFF0C\u4E0E\u4E4B\u7ED3\u5408\u53EF\u4EE5\u6781\u5927\u63D0\u9AD8\u7CFB\u7EDF\u53EF\u7528\u6027</p></li></ul>`,8),t=[p];function c(l,i){return s(),a("div",null,t)}var d=n(o,[["render",c],["__file","background.html.vue"]]);export{d as default};