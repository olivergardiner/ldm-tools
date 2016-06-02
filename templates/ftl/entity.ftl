<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Overview</title>
		<link rel="stylesheet" type="text/css" href="<#if rootPath?length gt 1>${rootPath}</#if>stylesheet.css" title="Style">
	</head>
	<body>
		<!-- <div class="topNav"></div> -->
		<div class="header">
			<div class="subTitle">${qualifiedPackageName}</div>
			<h2 title="Class NodeModel" class="title">${class} ${entityName}</h2>
		</div>
		<div class="contentContainer">
			<ul class="inheritance">
				<li>${stereotype}</li>
				<li>
					<#list entityHierarchy as ancestor>
					<ul class="inheritance">
						<li><#if ancestor.entityPath != ""><a href="${rootPath}${ancestor.entityPath}.html">${ancestor.qualifiedEntityName}</a><#else>${ancestor.qualifiedEntityName}</#if></li>
						<li>
					</#list>
					<#list entityHierarchy as ancestor>
						</li>
					</ul>
					</#list>
				</li>
			</ul>
			<div class="description">
				<ul class="blockList">
					<li class="blockList">
						<hr>
						<br>
						<pre><#if isAbstract>abstract </#if><span class="strong">${entityName}</span><#if parent != ""> extends ${parent}</#if></pre>
						<div class="block">
							<#list comments as comment><p>${comment}</p></#list>
						</div>
					</li>
				</ul>
			</div>
			<div class="summary">
				<ul class="blockList">
					<li class="blockList">
<!-- ========== ATTRIBUTE SUMMARY =========== -->
						<ul class="blockList">
							<li class="blockList">
								<h3>Attribute Summary</h3>
								<#if attributes?size gt 0>
									<table class="overviewSummary" border="0" cellpadding="3" cellspacing="0" summary="Attribute Summary table, listing attributes and an explanation">
										<caption><span>Attributes</span><span class="tabEnd">&nbsp;</span></caption>
										<tbody>
											<tr>
												<th class="colFirst" scope="col">Modifier and Type</th>
												<th class="colLast" scope="col">Attribute and Description</th>
											</tr>
											<#assign odd=true>
											<#list attributes?sort_by("name") as attribute>
												<#assign odd=!odd>
												<tr class="<#if odd>altColor<#else>rowColor</#if>">
													<td class="colFirst"><code>${attribute.stereotypes}</code>&nbsp;<div class="block"><code><#if attribute.typePath?length gt 0><a href="${rootPath}${attribute.typePath}.html"></#if>${attribute.type}<#if attribute.typePath?length gt 0></a></#if>${attribute.cardinality}</code></div></td>
													<td class="colLast"><code><strong>${attribute.name}</strong></code>&nbsp;<div class="block"><#list attribute.comments as comment>${comment}</#list></div></td>
												</tr>
											</#list>
										</tbody>
									</table>
								</#if>

								<#list inheritedAttributes as superClass>
									<#if superClass.attributes?size gt 0>
									<ul class="blockList">
										<li class="blockList">
											<h3>Attributes inherited from <a href="${rootPath}${superClass.entityPath}.html" title="${superClass.entityName}">${superClass.entityName}</a></h3>
											<#assign separator="">
											<#list superClass.attributes as attribute><code>${separator}${attribute}</code><#assign separator=", "></#list>
										</li>
									</ul>
									</#if>
								</#list>
							</li>
						</ul>
<!-- ========== ATTRIBUTE SUMMARY =========== -->
<!-- ========== ASSOCIATION SUMMARY =========== -->
						<ul class="blockList">
							<li class="blockList">
								<h3>Association Summary</h3>
								<#if associations?size gt 0>
									<table class="overviewSummary" border="0" cellpadding="3" cellspacing="0" summary="Association Summary table, listing associations and an explanation">
										<caption><span>Associations</span><span class="tabEnd">&nbsp;</span></caption>
										<tbody>
											<tr>
												<th class="colFirst" scope="col">Entity and Cardinality</th>
												<th class="colLast" scope="col">Association and Description</th>
											</tr>
											<#assign odd=true>
											<#list associations?sort_by("name") as association>
												<#assign odd=!odd>
												<tr class="<#if odd>altColor<#else>rowColor</#if>">
													<td class="colFirst"><code><#if association.typePath?length gt 0><a href="${rootPath}${association.typePath}.html"></#if>${association.type}<#if association.typePath?length gt 0></a></#if> ${association.cardinality}</code></td>
													<td class="colLast"><code><strong>${association.name}</code></strong>&nbsp;<div class="block"><#list association.comments as comment>${comment}</#list></div></td>
												</tr>
											</#list>
										</tbody>
									</table>
								</#if>

								<#list inheritedAssociations as superClass>
									<#if superClass.associations?size gt 0>
									<ul class="blockList">
										<li class="blockList">
											<h3>Associations inherited from <a href="${rootPath}${superClass.entityPath}.html" title="${superClass.entityName}">${superClass.entityName}</a></h3>
											<#assign separator="">
											<#list superClass.associations as association><div class="block"><code>${association}</code></div><#assign separator=", "></#list>
										</li>
									</ul>
									</#if>
								</#list>
							</li>
						</ul>
<!-- ========== ASSOCIATION SUMMARY =========== -->
					</li>
				</ul>
			</div>
		</div>
		<!-- <div class="bottomNav"></div> -->
	</body>
</html>