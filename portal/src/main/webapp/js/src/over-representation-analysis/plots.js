/**
 * Created by suny1 on 5/28/15.
 */
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/******************************************************************************************
 * Creating RPPA plots for "enrichments" tab
 * @author Yichao Sun
 *
 * This code performs the following functions:
 * 1. Generates boxplots for altered and unaltered group using D3 when user expanding any row in the table
 * 2. Annotates the plots with caseId, alteration details, specific value
 ******************************************************************************************/

var orPlots = (function() {

    var div_id, gene, profile_type, profile_id;
    var dotsArr = [], xAxisTextSet = ["Altered", "Unaltered"];

    var elem = {
            svg : "",
            xScale : "",
            yScale : "",
            xAxis : "",
            yAxis : "",
            dotsGroup : ""   //Group of single Dots
        }, settings = {
            canvas_width: 720,
            canvas_height: 600,
            dots_fill_color: "#58ACFA",
            dots_stroke_color: "#0174DF"
        };

    var data_process = function(result) {

        dotsArr = [];
        dotsArr.length = 0;

        $.each(Object.keys(result[gene]), function(index, _sampleId) {
            var _obj = result[gene][_sampleId];
            var _datum = {};
            if (!isNaN(_obj[profile_id])) {
                if ($.inArray(_sampleId, window.PortalGlobals.getAlteredSampleIdArray()) !== -1) {
                    _datum.x_val = 0;
                } else {
                    _datum.x_val = 1;
                }
                _datum.y_val = parseFloat(_obj[profile_id]);
                _datum.case_id = _sampleId;
                dotsArr.push(_datum);
            }
        });

        generate_plots();

    }

    var generate_plots = function() {

        $("#" + div_id).empty();

        //init canvas
        elem.svg = d3.select("#" + div_id)
            .append("svg")
            .attr("width", settings.canvas_width)
            .attr("height", settings.canvas_height);

        //init axis scales
        elem.xScale = d3.scale.linear() //x axis scale
            .domain([-0.7, 1.7])
            .range([100, 600]);
        var _yValArr = []; //y axis scale
        $.each(dotsArr, function(index, val){
            _yValArr.push(val.y_val);
        });
        var _results = or_util.analyse_data(_yValArr);
        elem.yScale = d3.scale.linear()
            .domain([_results.min, _results.max])
            .range([520, 20]);
        elem.xAxis = d3.svg.axis()
            .scale(elem.xScale)
            .orient("bottom");
        elem.yAxis = d3.svg.axis()
            .scale(elem.yScale)
            .orient("left");

        //Draw axis
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 520)")
            .attr("class", "rppa-plots-x-axis-class")
            .call(elem.xAxis.ticks(xAxisTextSet.length))
            .selectAll("text")
            .data(xAxisTextSet)
            .style("font-family", "sans-serif")
            .style("font-size", "13px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black")
            .text(function(d){ return d; });
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 20)")
            .call(elem.xAxis.orient("bottom").ticks(0));
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(100, 0)")
            .attr("class", "rppa-plots-y-axis-class")
            .call(elem.yAxis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "13px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(600, 0)")
            .call(elem.yAxis.orient("left").ticks(0));

        //Append Axis Titles
        var axisTitleGroup = elem.svg.append("svg:g");
        axisTitleGroup.append("text")
            .attr("class", "rppa-plots-x-axis-title")
            .attr("x", 350)
            .attr("y", 580)
            .style("text-anchor", "middle")
            .style("font-size", "13px")
            .text("something");
        axisTitleGroup.append("text")
            .attr("class", "rppa-plots-y-axis-title")
            .attr("transform", "rotate(-90)")
            .attr("x", -270)
            .attr("y", 45)
            .style("text-anchor", "middle")
            .style("font-size", "13px")
            .text("something");

        //draw dots
        elem.dotsGroup = elem.svg.append("svg:g");
        elem.dotsGroup.selectAll("path").remove();
        var ramRatio = 80;  //Noise
        elem.dotsGroup.selectAll("path")
            .data(dotsArr)
            .enter()
            .append("svg:path")
            .attr("transform", function(d){
                return "translate(" + (elem.xScale(d.x_val) + (Math.random() * ramRatio - ramRatio/2)) + ", " + elem.yScale(d.y_val) + ")";
            })
            .attr("d", d3.svg.symbol()
                .size(20)
                .type("circle"))
            .attr("fill", settings.dots_fill_color)
            .attr("stroke", settings.dots_stroke_color)
            .attr("stroke-width", "1.2");


    }

    return {
        init: function(_div_id, _gene, _profile_type, _profile_id) {

            div_id = _div_id;
            gene = _gene;
            profile_type = _profile_type;
            profile_id = _profile_id;

            var params_get_profile_data = {
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                gene_list: gene,
                genetic_profile_id: profile_id,
                case_set_id: window.PortalGlobals.getCaseSetId(),
                case_ids_key: window.PortalGlobals.getCaseIdsKey()
            }
            $.post("getProfileData.json", params_get_profile_data, data_process, "json");

        }
    };

}());