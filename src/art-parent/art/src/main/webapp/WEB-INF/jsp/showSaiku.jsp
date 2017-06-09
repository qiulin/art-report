<%-- 
    Document   : showSaiku
    Created on : 01-Jun-2017, 10:35:55
    Author     : Timothy Anyona
--%>

<%-- https://stackoverflow.com/questions/5771742/underscore-js-templates-within-jsp --%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.text.loading" var="loadingText"/>
<spring:message code="page.title.analytics" var="pageTitle"/>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta http-equiv="cache-control" content="max-age=0" />
    <meta http-equiv="cache-control" content="no-cache" />
    <meta http-equiv="expires" content="0" />
    <meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
    <meta http-equiv="pragma" content="no-cache" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

    <title>ART - ${pageTitle}</title>

    <!--[if lt IE 9]><script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/svgweb/svg.js" data-path="js/svgweb/"></script><![endif]-->
    <meta name="svg.render.forceflash" content="true">
	
	
    <link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/css/saiku/src/bootstrap.css"/>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/css/saiku/src/bootstrap2.css"/>
	
    <!-- Blueprint CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/js/tourist/tourist.css" type="text/css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/js/qtip/jquery.qtip.min.css" type="text/css"/>

    <!--  jQuery CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/css/jquery/spectrum.css" type="text/css" media="all"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/css/jquery/jquery-ui.css" type="text/css" media="all" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/css/saiku/src/giveitsomestyle.css" type="text/css" media="screen"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/js/fancybox/jquery.fancybox.css" type="text/css" media="screen" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/js/jquery/jquery.contextMenu.css" type="text/css" media="screen" />
    <!-- Saiku CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/css/saiku/src/styles.css" type="text/css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/css/saiku/src/saiku.table.css" type="text/css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/saiku/css/saiku/src/saiku.dropzone.css" type="text/css">
	
    <!-- CHOSEN CSS PLACEHOLDER-->

	<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
	
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-4.5.0/css/font-awesome.min.css">
<!--	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-hover-dropdown-2.0.3.min.js"></script>-->
	
	<!-- https://stackoverflow.com/questions/23509122/bootstrap-modal-overflowing-my-table-row -->
	<style>
		.modal-body {
			overflow-x: auto;
		}
	</style>

</head>
<body>
<div id="header" class="hide" style="display:none"></div>

<div class="processing_container"></div>
<div class="processing">
    <div class="processing_inner">
        <span class="saiku_logo">&nbsp;</span>
        <div class="processing_content">
            <span class="processing_image">&nbsp;</span>
            <span class="processing_message">${loadingText}...</span>
        </div>
    </div>
</div>

<!--
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku///www.google.com/jsapi" defer></script>
<script src="${pageContext.request.contextPath}/saiku///maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true&libraries=visualization&sensor=false&callback=initialize" defer></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/saiku///cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.3/leaflet.css">
<script src="${pageContext.request.contextPath}/saiku///cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.3/leaflet.js"></script>
-->

<!--MAP PLACEHOLDER-->

<!--  jQuery 1.7.2 , jQuery UI 1.8.14-->
<script src="${pageContext.request.contextPath}/saiku/js/jquery/jquery.min.js" type="text/javascript"></script>
<script src="${pageContext.request.contextPath}/saiku/js/jquery/jquery-ui.min.js" type="text/javascript"></script>
<script src="${pageContext.request.contextPath}/saiku/js/jquery/jquery.contextMenu.js" type="text/javascript"></script>
<script src="${pageContext.request.contextPath}/saiku/js/jquery/jquery-mobile-touch.js" type="text/javascript"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/jquery/jquery.blockUI.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/jquery/spectrum.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/fancybox/jquery.fancybox.pack.js"></script>


<!-- CHOSEN PLACEHOLDER -->
<script type="text/javascript">

    // Replace the normal jQuery getScript function with one that supports
    // debugging and which references the script files as external resources
    // rather than inline.
    jQuery.extend({
        getScript: function(url, callback) {
            var head = document.getElementsByTagName("head")[0];
            var script = document.createElement("script");
            script.src = url;

            // Handle Script loading
            {
                var done = false;

                // Attach handlers for all browsers
                script.onload = script.onreadystatechange = function(){
                    if ( !done && (!this.readyState ||
                            this.readyState == "loaded" || this.readyState == "complete") ) {
                        done = true;
                       if (callback)
                            callback();

                        // Handle memory leak in IE
                        script.onload = script.onreadystatechange = null;
                    }
                };
            }

            head.appendChild(script);

            // We handle everything using the script element injection
            return undefined;
        }
    });
</script>
<!-- Backbone.js and deps -->

<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/backbone/underscore.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/backbone/json2.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/backbone/backbone.js"></script>
<script src="${pageContext.request.contextPath}/saiku/js/backbone/backbone-model-file-upload.js" ></script>
<script src="${pageContext.request.contextPath}/saiku/js/notify/notify.min.js"></script>
<script src="${pageContext.request.contextPath}/saiku/js/fitvids/jquery.fitvids.js"></script>
<script src="${pageContext.request.contextPath}/saiku/js/notify/styles/bootstrap/notify-bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/saiku/js/qtip/imagesloaded.pkg.min.js"></script>
<script src="${pageContext.request.contextPath}/saiku/js/qtip/jquery.qtip.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/tourist/tourist.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/intro/intro.min.js"></script>
<!-- Loggly error logging service -->
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/logger/janky.post.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/logger/Logger.js" defer></script>

<!-- D3 Library -->
<script src="${pageContext.request.contextPath}/js/d3-3.5.17/d3.min.js"></script>

<!-- Saiku Project -->
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/Settings.js"></script>

<!-- CUT HERE -->
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/SaikuOlapQuery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/DateFilter.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/Level.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/render/SaikuRenderer.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/render/SaikuTableRenderer.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/render/SaikuChartRenderer.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/Dimension.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/DimensionList.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/Toolbar.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/Upgrade.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/Modal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/MDXModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/SelectionsModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/DrillthroughModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/DrillAcrossModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/PermissionsModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/DemoLoginForm.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/LoginForm.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/AboutModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/OverwriteModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/AddFolderModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/FilterModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/CustomFilterModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/CalculatedMemberModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/ParentMemberSelectorModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/DataSourcesModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/QueryToolbar.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/WorkspaceToolbar.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/WorkspaceDropZone.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/Table.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/Workspace.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/DeleteRepositoryObject.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/MoveRepositoryObject.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/OpenQuery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/SaveQuery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/OpenDialog.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/TabSet.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/Tab.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/Repository.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/Result.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/QueryAction.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/QueryScenario.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/Query.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/SessionErrorModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/WarningModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/TitlesModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/GrowthModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/FormatAsPercentageModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/Session.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/SplashScreen.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/SessionWorkspace.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/Member.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/Plugin.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/Settings.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/License.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/models/DataSources.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/Saiku.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/DateFilterModal.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/adapters/SaikuServer.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/routers/QueryRouter.js"></script>
<!--<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/views/Tour.js"></script>-->

<!-- END CUT HERE -->

<!-- Saiku Minified - remove all of the above and uncomment the following -->
<!--
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/saiku.min.js"></script>
-->

<!--
<script src="${pageContext.request.contextPath}/saiku/js/leaflet/leaflet-heat.js"></script>
-->

<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/plugins/I18n/plugin.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/plugins/Intro/plugin.js"></script>
<!-- Saiku plugins -->


<!-- FILTER PLUGIN disabled by default
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/plugins/filters/filterconfig.js" defer></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/plugins/filters/plugin_disabled.js" defer></script>
-->

<!-- CCC dependencies -->
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/plugins/CCC_Chart/def.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/plugins/CCC_Chart/protovis.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/plugins/CCC_Chart/protovis-msie.js"></script>
<!-- https://groups.google.com/a/saiku.meteorite.bi/forum/#!topic/dev/zUindH6t2y4 -->
<!--<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/plugins/CCC_Chart/cdo.js"></script>-->
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/plugins/CCC_Chart/pvc.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/plugins/CCC_Chart/jquery.tipsy.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/plugins/CCC_Chart/tipsy.js"></script>
<%-- https://stackoverflow.com/questions/1842308/having-link-in-body --%>
	<%-- https://stackoverflow.com/questions/6236097/is-link-not-rel-stylesheet-allowed-to-be-used-in-body --%>
	<link type="text/css" href="${pageContext.request.contextPath}/saiku/js/saiku/plugins/CCC_Chart/tipsy.css" rel="stylesheet"/>

<!--<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/../js/saiku/render/SaikuRenderer.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/../js/saiku/render/SaikuTableRenderer.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/../js/saiku/render/SaikuChartRenderer.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/../js/saiku/embed/SaikuEmbed.js"></script>-->

<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/saiku/embed/SaikuEmbed.js"></script>
<!-- ACE Editor for MDX -->
<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/js/ace/ace.js" charset="utf-8"></script>


<!--<script type="text/javascript" src="${pageContext.request.contextPath}/saiku/../js/jquery-1.12.4.min.js"></script>-->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.3.6/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>


<!-- https://sauce-dallas.blogspot.co.ke/2014/05/saiku-analytics-ui-customization-and.html -->

<!-- Templates -->
<script type="text/x-jquery-tmpl" id="template-toolbar">
		<div id="toolbar">
            <ul>
                    <li><a id='new_query' href='#new_query' title='New query' class='new_tab i18n sprite'></a></li>
                    <li class='separator'>&nbsp;</li>
                    <li><a id='open_query' href='#open_query' title='Open query' class='open_query i18n sprite'></a></li>
                    <\% if (!data.buttons) { %>
                    <li class='separator'>&nbsp;</li>
                    <li><a id='logout' href='#art_home' title='ART Home' class='logout i18n sprite'></a></li>
					<li class='separator'>&nbsp;</li>
					<li><a id='saiku_home' href='#saiku_home' title='Analytics Home' class='i18n'><i class="fa fa-home" data-href="#saiku_home"></i></a></li>
					<li class='separator'>&nbsp;</li>
                    <\% } %>
            </ul>

            <\%= data.logo %>
			</div>
        </script>

<script type="text/x-jquery-tmpl" id="template-upgrade">
            <div class="upgradeheader">
                You are using Saiku Community Edition, please consider <a target="_blank" href="http://saiku.meteorite.bi/support">purchasing support, or entering a sponsorship agreement with us</a> to support development. <a href="mailto:info@meteorite.bi?subject=Supporting Saiku">info@meteorite.bi</a><span class="close_tab sprite"></span>
            </div>
        </script>
<script type="text/x-jquery-tmpl" id="template-logincount">
            <div class="upgradeheader">

            </div>
        </script>
<script type="text/x-jquery-tmpl" id="template-cubes">
            <div >
            <select  id="cubesselect" class="form-control cubes ui-widget">
                <option value="" class="i18n">Select a cube</option>
                <\% _.each(connections, function(connection) { %>
                    <\% _.each(connection.catalogs, function(catalog) { %>
                        <\% _.each(catalog.schemas, function(schema) {
                            if (schema.cubes.length > 0) { %>
                                <optgroup label="<\%= ( schema.name != '' ? schema.name : catalog.name) +'  (' + connection.name + ')' %>">
                                <\% _.each(schema.cubes, function(cube) { %>
                                    <\% if (typeof cube["visible"] == "undefined" || cube["visible"]) { %>
                                        <option value="<\%= connection.name %>/<\%= catalog.name %>/<\%=  ((schema.name == '' || schema.name == null) ? 'null' : schema.name)  %>/<\%= encodeURIComponent(cube.name) %>"><\%= ((cube.caption == '' || cube.caption == null) ? cube.name : cube.caption) %></option>
                                    <\% } %>
                                <\% }); %>
                                </optgroup>
                            <\% } %>
                        <\% }); %>
                    <\% }); %>
                <\% }); %>
            </select>
            </div>
        </script>

<script type="text/x-jquery-tmpl" id="template-dimensions">
        <ul>
        <\% _.each(dimensions, function(dimension) { %>
        <\% if (dimension.name != 'Measures' && (typeof dimension["visible"] == "undefined" || dimension["visible"])) { %>
            <li class='parent_dimension'>
                <span class="root collapsed sprite"></span>
                <a class="folder_collapsed sprite" href="#" title="<\%= dimension.description ? dimension.description : dimension.caption %>"><\%= dimension.caption %></a>
                <ul>
                <\% _.each(dimension.hierarchies, function(hierarchy) { %>
                    <\% if (typeof hierarchy["visible"] == "undefined" || hierarchy["visible"]) { %>
                        <\% if ((Settings.DIMENSION_HIDE_HIERARCHY === 'NONE' && dimension.hierarchies.length > 1)
                        		|| (Settings.DIMENSION_HIDE_HIERARCHY === 'SINGLE_LEVEL' && ((Settings.DIMENSION_SHOW_ALL && hierarchy["levels"].length > 3) || (!Settings.DIMENSION_SHOW_ALL && hierarchy["levels"].length > 2)))) { %>
                            <li class="hierarchy hide">
                                <a class="dimension" title="<\%= hierarchy.description ? hierarchy.description : hierarchy.caption %>" href="#"  hierarchy="<\%= hierarchy.name %>"><\%= hierarchy.caption %></a>
                            </li>
                        <\% } %>
                        <ul class="d_hierarchy" hierarchy="<\%= hierarchy.uniqueName %>" hierarchycaption="<\%= hierarchy.caption %>">
                        <div class="hgroup treehidden"><\%= hierarchy.caption %></div>
                        <\% _.each(hierarchy.levels, function(level) { %>
                            <\% if ((Settings.DIMENSION_SHOW_ALL || level.name != "(All)") && (typeof level["visible"] == "undefined" || level["visible"])) { %>
                                <li class="hide d_level">
                                <a title="<\%= (Settings.DIMENSION_HIDE_HIERARCHY === 'SINGLE_LEVEL' || Settings.DIMENSION_HIDE_HIERARCHY === 'ALL') && dimension.hierarchies.length > 1? hierarchy.caption + ' - ' : '' %><\%= level.description ? level.description : level.caption %>" level="<\%= level.name %>" hierarchy="<\%= hierarchy.uniqueName %>"
                                    class="level" href="#<\%= encodeURIComponent(hierarchy.uniqueName) %>/<\%= encodeURIComponent(level.name) %>"><\%= level.caption %></a>
                                </li>
                            <\% } %>
                        <\% }); %>
                        </ul>
                    <\% } %>
                <\% }); %>
                </ul>
        <\% } %>
        <\% }); %>
        </ul>
        </script>

<script type="text/x-jquery-tmpl" id="template-measures">
                <ul>
                <\% var g = _.groupBy(measures, 'measureGroup'); %>
                <\% _.each(g, function(group, key) { %>
                    <\% if(key != "null") { %>
                    <li class='parent_dimension'>
                    <span class="root <\%= Settings.MEASURE_GROUPS_COLLAPSED ? 'collapsed' : 'expand' %> sprite"></span>
                      <a class="folder_expand sprite" href="#" title="<\%= key %>"><\%= key %></a>
                        <ul>
                        <\% _.each(group, function(measure) { %>
                        <\% if (typeof measure["visible"] == "undefined" || measure["visible"]) { %>
                        <li class="d_measure <\%= Settings.MEASURE_GROUPS_COLLAPSED ? 'hide' : '' %>"><a title="<\%= measure.description ? measure.description : measure.uniqueName %>"
                                measure="<\%= measure.name %>" type="EXACT"
                            class="measure" href="#Measures/member/<\%= encodeURIComponent(measure.uniqueName) %>"><\%= measure.caption %></a>
                        </li>

                        <\% } %>
                        <\% }); %>
                        </ul>
                        </li>
                    <\% } else {%>
                    <li class='parent_dimension'>
                    <span class="root <\%= Settings.MEASURE_GROUPS_COLLAPSED ? 'collapsed' : 'expand' %> sprite"></span>
                      <a class="folder_expand sprite" href="#" title=" "> </a>
                        <ul>
                    <\% _.each(group, function(measure) { %>

                        <li class="d_measure <\%= Settings.MEASURE_GROUPS_COLLAPSED ? 'hide' : '' %>"><a title="<\%= measure.description ? measure.description : measure.uniqueName %>"
                                measure="<\%= measure.name %>" type="EXACT"
                            class="measure" href="#Measures/member/<\%= encodeURIComponent(measure.uniqueName) %>"><\%= measure.caption %></a>
                        </li>

                        <\% }); %>
                      </ul>
                      </li>
                    <\% } %>
                <\% }); %>
                </ul>
        </script>
<script type="text/x-jquery-tmpl" id="template-calculated-measures">
        <ul>
            <li>
                <span class="root expand sprite"></span>
                   <a href="#" title="Calculated Measures" class="folder_expand sprite i18n">Calculated Measures</a>
                <\% if (measures) { %>
                <ul>
                <\% _.each(measures, function(measure) { %>

                        <li class="d_measure"><a title="<\%= measure.name %>"
                                measure="<\%= measure.name %>" type="CALCULATED"
                            class="measure" href="#Measures/member/<\%= encodeURIComponent(measure.uniqueName) %>"><\%= measure.name %></a>
                        </li>

                <\% }); %>
                <\% } %>
                </ul>
            </li>
        </ul>
        </script>
<script type="text/x-jquery-tmpl" id="template-calculated-member">
    <\% if (member) { %>
        <li class="hide d_level dimension-level-calcmember">
            <a title="<\%= member.name %>" level="<\%= member.name %>" hierarchy="<\%= member.hierarchyName %>" uniquename="<\%= member.uniqueName %>" class="level level-calcmember" href="#<\%= encodeURIComponent(member.hierarchyName) %>/<\%= encodeURIComponent(member.name) %>" style="color: #1e7145;"><\%= member.name %></a>
        </li>
    <\% } %>
</script>
<script type="text/x-jquery-tmpl" id="template-workspace">

				
        <div class="workspace">
            <div class="workspace_inner">
                <div class="query_toolbar"></div>
                <div class="workspace_toolbar"></div>
                <div class="upgrade"></div>
                <div class="parameter_input" />
                <div class="workspace_editor hide">
                    <div id='mdx_editor' class='mdx_input hide'>
                    </div>
                    <span class="editor_info hide"></span>

                </div>
                <div id="query_processing" class="query_processing" style="display:none;"><span class="processing_image">&nbsp;&nbsp;</span> <span class="i18n">Running query...</span>  [&nbsp;<a class="cancel i18n" href="#cancel">Cancel</a>&nbsp;]</div>

                <div class="workspace_results_info" align="right" />
                <div class="workspace_results_titles" align="center"/>
                <div class="workspace_results">

                </div>
            </div>
        </div>
        <div class="sidebar">
            <div>
                <h3 class="top i18n">Cubes</h3>
                <div class="refresh_cubes_nav">
                    <a id="refresh_icon" href="#refresh_cubes"
                        class="i18n button refresh_cubes"
                        title="Refresh Cubes (Clear Cache)"></a>
                </div>
                <div class="admin_console_nav"></div>
            </div>
            <div class="sidebar_inner">
                <\%= cube_navigation %>
            </div>
            <div class="metadata_attribute_wrapper">
                <\%=  _.template($("#template-attributes").html())() %>
            </div>

        </div>

        <div class="sidebar_separator"></div>
        <div class="clear"></div>
		

        </script>

<script type="text/x-jquery-tmpl" id="template-attributes">

        <div class="metadata_attribute_container">
            <h3 class="measure_caption"><span class="i18n">Measures</span><a class="button addMeasure i18n" style="display:none;">Add</a></h3>
            <div class="sidebar_inner measure_tree">
                <\% if (typeof cube != "undefined" && cube && cube.dimensions) { %>
                    <\%= _.template($("#template-measures").html(), { measures: cube.measures }) %>
                <\% } else { %>
                    <span class="i18n loading hide">Loading...</span>
                <\% } %>
                <div class="calculated_measures" style="display:none;"></div>
                <div class="calculated_members" style="display:none;"></div>
            </div>
            <h3 class="i18n dimension_header">Dimensions</h3>
            <div class="sidebar_inner dimension_tree">
                <\% if (typeof cube != "undefined" && cube && cube.dimensions) { %>
                    <\%=  _.template($("#template-dimensions").html(), { dimensions: cube.dimensions }) %>
                <\% } else { %>
                    <span class="i18n loading hide">Loading...</span>
                <\% } %>

            </div>
        </div>

        </script>


<script type="text/x-jquery-tmpl" id="template-workspace-dropzones">
        <div class="workspace_fields disabled">
            <div class="fields_list details_fields" title="DETAILS">
                <div class="fields_list_header measure_fields disabled_toolbar"><span class="i18n">Measures</span><span class="dropdown"></span></div>
                <div class="fields_list_body details">
                    <ul class="connectable"></ul>
                </div>
                <span class="clear_axis i18n hide" title="Clear Axis"></span>
                <div class="clear"></div>
            </div>
            <div class="fields_list columns_fields" title="COLUMNS">
                <div class="fields_list_header axis_fields_header disabled_toolbar"><span class="i18n">Columns</span><span class="dropdown"></span></div>
                <div class="fields_list_body columns axis_fields">
                    <ul class="connectable"></ul>
                </div>
                <span class="clear_axis i18n hide" title="Clear Axis"></span>
                <div class="clear"></div>
            </div>
            <div class="fields_list rows_fields" title="ROWS">
                <div class="fields_list_header axis_fields_header disabled_toolbar"><span class="i18n">Rows</span><span class="dropdown"></span></div>
                <div class="fields_list_body rows axis_fields">
                    <ul class="connectable"></ul>
                </div>
                <span class="clear_axis i18n hide" title="Clear Axis"></span>
                <div class="clear"></div>
            </div>
            <div class="fields_list filter_fields" title="FILTER">
                <div class="fields_list_header axis_fields_header disabled_toolbar"><span class="i18n">Filter</span><span class="dropdown"></span></div>
                <div class="fields_list_body filter axis_fields">
                    <ul class="connectable"></ul>
                </div>
                <span class="clear_axis i18n hide" title="Clear Axis"></span>
                <div class="clear"></div>
            </div>
        </div>
        </script>

<script type="text/x-jquery-tmpl" id="template-workspace-toolbar">
        <ul>
            <li><a href="#open_query"
                class="i18n open button sprite"
                title="Open query" id="new_icon"></a></li>
            <li><a href="#save_query" id="save_icon"
                class="i18n save button disabled_toolbar sprite"
                title="Save query"></a></li>
            <li class="seperator"><a href="#new_query"
                class="i18n new disabled_toolbar button sprite"
                title="Reset query"></a></li>
            <li><a href="#edit_query"
                class="i18n edit button on disabled_toolbar sprite"
                title="Edit query" id="edit_icon"></a></li>
            <li class="seperator"><a href="#run_query" id="run_icon"
                class="i18n run button disabled_toolbar sprite"
                title="Run query"></a></li>
            <li><a href="#automatic_execution" id="automatic_icon"
                class="i18n auto button disabled_toolbar sprite"
                title="Automatic execution"></a></li>
<!--
            <li><a href="#toggle_fields"
                class="i18n toggle_fields button sprite"
                title="Toggle fields"></a></li>
            <li><a href="#toggle_sidebar"
                class="i18n toggle_sidebar button sprite"
                title="Toggle sidebar"></a></li>
-->
            <li class="seperator"><a href="#group_parents" id="group_icon"
                class="i18n group_parents button disabled_toolbar sprite"
                title="Hide Parents"></a></li>
            <li><a href="#non_empty" id="non_empty_icon"
                class="i18n non_empty button disabled_toolbar sprite"
                title="Non-empty"></a></li>
            <li><a href="#swap_axis" id="swap_axis_icon"
                class="i18n swap_axis button disabled_toolbar sprite"
                title="Swap axis"></a></li>
            <li><a href="#show_mdx" id="show_mdx_icon"
                class="i18n mdx button disabled_toolbar sprite"
                title="Show MDX"></a></li>
            <!-- <li><a href="#explain_query"
                class="i18n explain_query button disabled_toolbar sprite"
                title="Show Explain Plan"></a></li> -->
            <li class="seperator"><a href="#zoom_mode" id="zoom_mode_icon"
                class="i18n table_mode zoom_mode button disabled_toolbar"
                title="Zoom into table"></a></li>
            <li class="seperator"><a href="#query_scenario"
                class="i18n table_mode query_scenario button disabled_toolbar sprite"
                title="Query Scenario"></a></li>
            <\% if (Settings.USER_TITLES == true) { %>
            <li class="seperator"><a href="#workspace_titles"
                class="i18n workspace_titles button disabled_toolbar sprite"
                title="Titles"></a></li>
                <\% } %>
	<li class="seperator"><a href="#drillacross" id="drillacross_icon"
class="i18n table_mode drillacross button disabled_toolbar sprite"
title="Drill across on cell"></a></li>
            <li class="seperator"><a href="#drillthrough" id="drillthrough_icon"
                class="i18n table_mode drillthrough button disabled_toolbar sprite"
                title="Drill through on cell"></a></li>
            <li><a href="#export_drillthrough" id="export_drillthrough_icon"
                class="i18n table_mode drillthrough_export button disabled_toolbar sprite"
                title="Export Drill-Through on cell to CSV"></a></li>
            <li class="seperator"><a href="#export_xls" id="export_xls_icon"
                class="i18n export_xls button disabled_toolbar sprite"
                title="Export XLS"></a></li>
            <li><a href="#export_csv" id="export_csv_icon"
                class="i18n export_csv button disabled_toolbar sprite"
                title="Export CSV"></a></li>
            <li><a href="#export_pdf" id="export_pdf_icon"
                class="i18n export_pdf button disabled_toolbar sprite"
                title="EXPERIMENTAL: Export PDF"></a></li>
            <li><a href="#switch_to_mdx" id="switch_to_mdx_icon"
                class="i18n switch_to_mdx button disabled_toolbar"
                title="Switch to MDX Mode"></a></li>
            <\% if (Settings.BIPLUGIN5 == true) { %>
            <li class="seperator"><a href="#about"
                class="i18n about button sprite"
                title="About"></a></li>
             <\% } %>
        </ul>

        </script>

<script type="text/x-jquery-tmpl" id="template-query-toolbar">
        <div class="query_toolbar_vertical">
        <ul class="renderer">
            <li><a href="#switch_render_button" id="table_icon"
                class="i18n render_table button disabled_toolbar on"
                title="Table Mode"></a></li>
            <li><a href="#switch_render_button" id="chart_icon"
                class="i18n render_chart button disabled_toolbar"
                title="Chart Mode"></a></li>
        </ul>
        <ul class='options table hide'>
                        <!--<li class="seperator_vertical label"><a class="i18n label disabled_toolbar">Options:</a></li> -->

            <li class="seperator_vertical"><a href="#spark_bar" id="spark_bar_icon"
                class="i18n spark_bar tablebutton button disabled_toolbar"
                title="Spark Bar"></a></li>
            <li><a href="#spark_line" id="spark_line_icon"
                class="i18n spark_line button disabled_toolbar"
                title="Spark Line"></a></li>
           <!-- <li><a href="#asdf"
                class="i18n button disabled_toolbar"
                title="Spark Lines">Show Totals</a></li> -->

        </ul>
        <ul class='options chart hide'>
            <li class="seperator_vertical"><a href ="#export_button"
                class="disabled_toolbar export_button menu button"><span class="i18n">Export</span><span class="dropdown"></span></a></li>
            <li><a href="#bar"
                class="i18n bar chartoption button disabled_toolbar"
                title="Bar"></a></li>
            <li><a href="#stackedBar"
                class="i18n stackedBar chartoption on button disabled_toolbar"
                title="Stacked Bar"></a></li>
            <li><a href="#stackedBar100"
                class="i18n stackedBar100 chartoption button disabled_toolbar"
                title="Bar 100%"></a></li>
            <li><a href="#multiplebar"
                class="i18n multiple chartoption button disabled_toolbar"
                title="Multiple Bar Chart"></a></li>
            <li><a href="#line"
                class="i18n line chartoption button disabled_toolbar"
                title="Line"></a></li>
            <li><a href="#area"
                class="i18n area chartoption button disabled_toolbar"
                title="Area"></a></li>
            <li><a href="#heatgrid"
                class="i18n heatgrid chartoption button disabled_toolbar"
                title="Heat Grid"></a></li>
            <li><a href="#treemap"
                class="i18n treemap chartoption button disabled_toolbar"
                title="Tree Map"></a></li>
            <li><a href="#sunburst"
                class="i18n sunburst chartoption button disabled_toolbar"
                title="Sunburst"></a></li>
            <li><a href="#multiplesunburst"
                class="i18n multiplesunburst chartoption button disabled_toolbar"
                title="Multi Sunburst"></a></li>
            <li><a href="#dot"
                class="i18n dot chartoption button disabled_toolbar"
                title="Dot"></a></li>
            <li><a href="#waterfall"
                class="i18n waterfall chartoption button disabled_toolbar"
                title="Waterfall"></a></li>
            <li><a href="#pie"
                class="i18n pie chartoption button disabled_toolbar"
                title="Pie"></a></li>
            <li><a href="#radar"
                class="i18n radar chartoption button disabled_toolbar"
                title="Radar"></a></li>
           <!-- <li><a href="#chart_editor"
                class="i18n custom_chart button disabled_toolbar"
                title="Custom">Custom</a></li> -->
        </ul>
        </div>
        </script>

<script type="text/x-jquery-tmpl" id="template-open-dialog">
            <div class="workspace" style="margin-left: -305px">
                <div class="workspace_inner" style="margin-left: 305px">
                    <div class="workspace_toolbar hide">
                    <ul>
                        <li class='for_queries hide'><a href="#open_query" class="run button sprite i18n" title="Run"></a></li>
                        <li class='for_queries hide'><a href="#edit_query" class="edit button sprite i18n" title="Edit"></a></li>
                        <li class='for_queries hide'><a href="#delete_query" class="delete button sprite i18n" title="Delete"></a></li>
                        <li class='for_queries hide'><a href="#edit_permissions" class="edit_permissions button sprite"></a></li>
                        <!--
                        <li class='for_folder hide'><a href="#edit_folder" class="edit_folder button"></a></li>
                        -->
                        <li class='for_folder hide'><a href="#delete_folder" class="delete button sprite"></a></li>
                        <li class='for_folder hide'><a href="#edit_permissions" class="edit_permissions button sprite"></a></li>
                    </ul>
                    </div>
                    <div class="workspace_results">
                    </div>
                </div>
            </div>

            <div class="sidebar queries" style="width: 300px">
                <h3 class="top" style="padding-top:3px;padding-bottom:2px;">
                <div class="form-inline">
                    <label class="i18n">Search:</label> &nbsp;
                        <input type="text" class="search_file2 form-control" style="padding-right:0px"></input>
                        <span class="cancel_search"></span>
                    </div>
                </h3>
                <div class="sidebar_inner">
                    <ul id="queries" class='RepositoryObjects'>
                        <li class="i18n">Loading...</li>
                    </ul>
                </div>
            </div>

            <div class="sidebar_separator"></div>
            <div class="clear"></div>
        </script>

<script type="text/x-jquery-tmpl" id="template-repository-objects">
            <\% _.each( repoObjects, function( entry ) { %>
                <\% if( entry.type === 'FOLDER' ) { %>
                   <\%= _.template($("#template-repository-folder").html())({ entry: entry }) %>
                <\% } else { %>
                    <\%= _.template($("#template-repository-file").html())({ entry: entry }) %>
                <\% } %>
            <\% } ); %>
        </script>
<script type="text/x-jquery-tmpl" id="template-repository-folder">
            <\% if(!Settings.REPOSITORY_LAZY) { %>
                <li class='folder'>
                    <div class='folder_row'>
                        <span class='sprite folder collapsed'></span>
                        <a href="#<\%= entry.path %>"><\%= entry.name %></a>
                    </div>

                    <ul class='hide folder_content'>

                        <\%= _.template($("#template-repository-objects").html())( { repoObjects: entry.repoObjects }) %>
                    </ul>
                </li>
            <\% } else { %>
                <li class='folder'>
                    <div class='folder_row'>
                        <span class='sprite folder collapsed'></span>
                        <a href="#<\%= entry.path %>"><\%= entry.name %></a>
                    </div>
                </li>
            <\% } %>
        </script>

<script type="text/x-jquery-tmpl" id="template-repository-folder-lazyload">
                <ul class='folder_content'>
                    <\%= _.template($("#template-repository-objects").html())( { repoObjects: repoObjects }) %>
                </ul>
        </script>

<script type="text/x-jquery-tmpl" id="template-repository-file">
            <li class='query'>
            <\% if (Settings.ICON_16) { %>
                <span class='icon_override' style='background-image: url(<\%= Settings.ICON_16 %>);'></span>
            <\% } else { %>
                <span class='icon'></span>
            <\% } %>
                <a href="#<\%= entry.path %>"><\%= entry.name %></a>
            </li>
        </script>

<script type="text/x-jquery-tmpl" id="template-selections">
            <div class="form-inline header_selections">
                <label for="filter_selections" class="i18n">Search:</label> &nbsp;<input name="filter_selections"
                id="filter_selections" disabled="true" type="text" class="form-control filterbox" /> <input class="i18n form-control search_term form_button" type="submit" value="Pre-Filter on Server" /><input class="i18n form-control clear_search form_button" type="submit" value="Clear Filter" /><br />
            </div>
            <div class="available_selections form-inline caption">
                <span class="i18n">Available members:</span><br/>
                    <div class="selection_options" ><ul></ul></div>
            </div>
            <div class="selection_buttons">
                <a class="form_button btn btn-default" href="#move_selection" id="add_members">&nbsp;&gt;&nbsp;
                </a><br><br>
                <a class="form_button btn btn-default" href="#move_selection" id="add_all_members">&gt;&gt;</a><br><br>
                <a class="form_button btn btn-default" href="#move_selection" id="remove_all_members">&lt;&lt;</a><br><br>
                <a class="form_button btn btn-default" href="#move_selection" id="remove_members">&nbsp;&lt;&nbsp;</a>
            </div>
            <div class="used_selections form-inline caption">
                <span class="i18n">Used members:</span>
				<span>
	 			<input class="selection_type selection_type_inclusion" type="radio" name="inclusion_type" value="INCLUSION" checked="true"><span class="i18n">Include</span>
	 			<input class="selection_type selection_type_exclusion" type="radio" name="inclusion_type"
	 			 value="EXCLUSION"><span class="i18n">Exclude</span>
	 			</span>
	 			<br/>
                    <div class="selection_options" ><ul></ul></div>
            </div>
            <div class="updown_buttons">
                <a class="form_button" href="#up" id="up_members">&nbsp;&uarr;&nbsp;</a><br><br>
                <a class="form_button" href="#down" id="down_members">&nbsp;&darr;&nbsp;</a><br><br>
            </div>

            <div class="options form-inline">
                <input class="form-control" name="show_unique" id="show_unique" type="checkbox" />&nbsp;<label
                for="show_unique"
                class="i18n">Show Unique Names</label><br />
                <input name="use_result" class="form-control" id="use_result" type="checkbox" />&nbsp;<label for="use_result"
                class="i18n">Use Result</label><br />
				<div id="div-totals-container">
                    <div class="totals_container">
                        <label class="i18n">Totals per metrics</label>
                        <input type="checkbox" id="per_metrics_totals_checkbox"/>
                        &nbsp;
                    </div>
                    <div class="totals_container all_metrics_container">
                        <strong>All Measures - </strong>
                        <label for="show_totals" class="i18n">Sub totals</label>
                        <select class="form-control" id="all_measures_select">
                            <option class="i18n" value="nil" selected="selected">None</option>
                            <option class="i18n" value="sum">Sum</option>
                            <option class="i18n" value="min">Min</option>
                            <option class="i18n" value="max">Max</option>
                            <option class="i18n" value="avg">Avg</option>
                        </select>
                    </div>
                </div>
				<div class="form-inline">
                <label for="parameter" class="parameter hidden i18n">Parameter Name</label>&nbsp;<input
                name="parameter" id="parameter" class="form-control parameter hidden" type="text" /></div>
            </div>
            <div class="info">
                <div><label for="items_size" class="i18n">Items</label>: <span class="items_size"></span></div>
                <div><span class="warning i18n"></span></div>
            </div>

            <div class="hint">
                <div><label for="search_limit" class="i18n">Display Limit</label>: <span class="members_limit"></span></div>
                <div><label for="search_limit" class="i18n">Filter Limit</label>: <span class="search_limit"></span></div>
            </div>

            <div class="autocomplete"></div>
        </script>
<script type="text/x-jquery-tmpl" id="template-selections-totals">
    <div class="totals_container per_metrics_container">
        <strong><\%= measure.caption %> - </strong>
        <label for="show_totals" class="i18n">Sub totals</label>
        <select class="form-control show_totals_select">
            <option class="i18n" value="nil" selected="selected">None</option>
            <option class="i18n" value="sum">Sum</option>
            <option class="i18n" value="min">Min</option>
            <option class="i18n" value="max">Max</option>
            <option class="i18n" value="avg">Avg</option>
        </select>
        &nbsp;
    </div>
</script>
<script type="text/x-jquery-tmpl" id="template-selections-options">
            <ul>
            <li class="i18n all_options"><input type="checkbox" class="check_all_option"
            id="check_all_option"><label for="check_all_option" class="i18n">All Members</label></li>
            <\%  var counter = 0;
                _.each(options, function(option) {
                    counter++; %>
                <\% if (!option.calc) { %>
                    <li class="option_value">
                        <input type="checkbox" class="check_option"
                            value="<\%= encodeURIComponent(option.obj ? option.obj.uniqueName : option.uniqueName) %>" name="selections_checker"
                            id="selections_checker<\%= counter %>"
                            label="<\%= encodeURIComponent(option.obj ? option.obj.caption : option.caption) %>" />
                            <label for="selections_checker<\%= counter %>" class="caption"><\%= option.obj ? option.obj.caption : option.caption
                            %></label>
                            <label for="selections_checker<\%= counter %>" class="unique"><\%= option.obj ? option.obj.uniqueName : option.uniqueName
                            %></label>
                    </li>
                <\% } else { %>
                    <li class="option_value">
                        <input type="checkbox" class="check_option cmember"
                            value="<\%= encodeURIComponent(option.obj ? option.obj.uniqueName : option.uniqueName) %>" name="selections_checker"
                            id="selections_checker<\%= counter %>"
                            label="<\%= encodeURIComponent(option.obj ? option.obj.caption : option.caption) %>" />
                            <label for="selections_checker<\%= counter %>" class="caption" style="color: blue;"><\%=
                            option.obj ? option.obj.caption : option.caption %></label>
                            <label for="selections_checker<\%= counter %>" class="unique" style="color: blue;"><\%=
                            option.obj ? option.obj.uniqueName : option.uniqueName %></label>
                    </li>
                <\% } %>
            <\% }); %>
            </ul>
        </script>


<script type="text/x-jquery-tmpl" id="template-permissions">
            <div class="permissions">

                        <label for="filter_roles" class="i18n">Add role permission:</label>
                        <br />
                        <form class="form-inline">
                            <input name="filter_roles" id="filter_roles" type="text" class="filterbox form-control" />
                            <input type="submit" value="Add" class="i18n add_role btn btn-primary" />
                        </form>
                        <input type="checkbox" class="acl" value="READ" /><span class="i18n">READ</span>
                        <input type="checkbox" class="acl" value="WRITE" /><span class="i18n">WRITE</span>
                        <input type="checkbox" class="acl" value="GRANT" /><span class="i18n">GRANT</span>
                <br />
                <br />
                <span class="i18n">Roles:</span>
                <div class="rolelist"></div>
                <br />
                <input type="submit" value="Remove permission" class="i18n btn btn-danger remove_acl" />
            </div>
            <br />
            <div class="private">
                <input type="checkbox" class="private" value="PRIVATE" /><span class="i18n">Keep this file private to me</span> (<b><\%= Saiku.session.username %></b>)
                <br /><br />
                <span class="private_owner" style="display:none;"><span class="i18n">Currently private to:</span>&nbsp;<span class="owner"></span></span>
            </div>
            <br />
        </script>
<script type="text/x-jquery-tmpl" id="template-permissions-rolelist">
            <select class="select_roles" multiple size="4">
                <\% _.each(roles, function(grants, role) {
                    var aclstring ="";
                     _.each(grants, function(grant, index) {
                        aclstring += (index > 0 ? (", " + grant) : grant);
                    });
                %>
                    <option value="<\%=role %>"><\%= role + " [" + aclstring + "]" %></option>
                <\% }); %>
            </select>
        </script>

<script type="text/x-jquery-tmpl" id="template-drillthrough">
            <div class="row_limit">
                <br />
                <span class="i18n">Row Limit: (0 = none)</span> <input id="maxrows" name="maxrows" class="maxrows" type="text" />
                <br />
                <span class="i18n"><b>CAUTION:</b> The number of rows can have a significant impact on the performance of the following action.</span>
            </div>
        </script>
<script type="text/x-jquery-tmpl" id="template-drillacross">
</script>
<script type="text/x-jquery-tmpl" id="template-drillthrough-list">
        <div class="sidebar" style="border:1px solid #CCCCCC">
            <h3><input type="checkbox" class="all_dimensions"/> &nbsp;<span class="i18n">Dimensions</span></h3>
            <div class="sidebar_inner dimension_tree"></div>
            <h3><input type="checkbox" class="all_measures"/> &nbsp;<span class="i18n">Measures</span></h3>
            <div class="sidebar_inner measure_tree"></div>
        </div>

        <div class="sidebar_separator"></div>
        <div class="clear"></div>
        </script>
<script type="text/x-jquery-tmpl" id="template-drillthrough-dimensions">
<ul>
<\% _.each(dimensions, function(dimension) { %>
<\% if (dimension.name != 'Measures' && (typeof dimension["visible"] == "undefined" || dimension["visible"])) { %>
<li class='parent_dimension'>
<span class="root collapsed sprite"></span>
<input type="checkbox" class="check_dimension" />
<a class="folder_collapsed sprite" href="#" title="<\%= dimension.description ? dimension.description : dimension.caption %>"><\%= dimension.caption %></a>
<ul>
<\% _.each(dimension.hierarchies, function(hierarchy) { %>
<\% if (typeof hierarchy["visible"] == "undefined" || hierarchy["visible"]) { %>
<\% if (dimension.hierarchies.length > 1) { %>
<li class="hierarchy hide">
<a class="dimension" title="<\%= hierarchy.description ? hierarchy.description : hierarchy.caption %>" href="#" key="<\%= dimension.name %>###<\%= hierarchy.name %>"><\%= hierarchy.caption %></a>
</li>
<\% } %>
<\% } %>
<\% _.each(hierarchy.levels, function(level) { %>
<\% if (typeof level["visible"] == "undefined" || level["visible"]) { %>
<\% if (level.caption != "(All)") { %>
<li class="hide"> &nbsp;&nbsp;&nbsp;&nbsp;
<input type="checkbox" class="check_level" value="<\%= level.uniqueName %>" key="<\%= dimension.name %>###<\%= hierarchy.name %>"> <\%= level.caption %></input>
</li>
<\% } %>
<\% } %>
<\% }); %>
<\% }); %>
</ul>
<\% } %>
<\% }); %>
</ul>
</script>

<script type="text/x-jquery-tmpl" id="template-drillthrough-measures">
<ul>
<li>
<span class="root expand sprite"></span>
<a href="#" title="Measures" class="folder_expand i18n sprite">Measures</a>
<ul>
<\% _.each(measures, function(measure) { %>
<\% if (typeof measure["visible"] == "undefined" || measure["visible"]) { %>
<\% if(typeof measure.calculated == "undefined" || (measure.calculated == false || allMeasures)) { %>
<li> &nbsp;&nbsp;&nbsp;&nbsp;
<input type="checkbox" class="check_level" value="<\%= measure.uniqueName %>" key="Measures###<\%= measure.name %>"> <\%= measure.caption %></input>
</li>
<\% } %>
<\% } %>
<\% }); %>
</ul>
</li>
</ul>
</script>

</body>
</html>