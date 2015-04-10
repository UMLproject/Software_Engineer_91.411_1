var graphs = {"Steps":
    {
        "type": "serial",
        "pathToImages": "http://cdn.amcharts.com/lib/3/images/",
        "categoryField": "date",
        "dataDateFormat": "YYYY-MM-DD HH",
        "categoryAxis": {
            "minPeriod": "hh",
            "parseDates": true
        },
        "chartCursor": {
            "categoryBalloonDateFormat": "JJ:NN"
        },
        "chartScrollbar": {},
        "trendLines": [],
        "graphs": [
            {
                "bullet": "round",
                "id": "AmGraph-1",
                "title": "John Doe",
                "valueField": "column-1"
            }
        ],
        "guides": [],
        "valueAxes": [
            {
                "id": "ValueAxis-1",
                "title": "Steps Taken"
            }
        ],
        "allLabels": [],
        "balloon": {},
        "legend": {
            "useGraphSettings": true
        },
        "titles": [
            {
                "id": "Title-1",
                "size": 15,
                "text": "Daily Steps"
            }
        ],
        "dataProvider": [
            {
                "column-1": "128",
                "date": "2014-03-01 08"
            },
            {
                "column-1": "523",
                "date": "2014-03-01 09"
            },
            {
                "column-1": "1136",
                "date": "2014-03-01 10"
            },
            {
                "column-1": "1325",
                "date": "2014-03-01 11"
            },
            {
                "column-1": "1458",
                "date": "2014-03-01 12"
            },
            {
                "column-1": "1847",
                "date": "2014-03-01 13"
            },
            {
                "column-1": "2271",
                "date": "2014-03-01 14"
            },
            {
                "column-1": "2403",
                "date": "2014-03-01 15"
            },
            {
                "column-1": "2965",
                "date": "2014-03-01 16"
            },
            {
                "column-1": "3127",
                "date": "2014-03-01 17"
            }
        ]
    },
    "Flights": {
        "type": "serial",
        "pathToImages": "http://cdn.amcharts.com/lib/3/images/",
        "categoryField": "category",
        "colors": [
            "#ae85c9",
            "#aab9f7",
            "#b6d2ff",
            "#c9e6f2",
            "#c9f0e1",
            "#e8d685",
            "#e0ad63",
            "#d48652",
            "#d27362",
            "#495fba",
            "#7a629b",
            "#8881cc"
        ],
        "startDuration": 1,
        "theme": "light",
        "categoryAxis": {
            "gridPosition": "start"
        },
        "trendLines": [],
        "graphs": [
            {
                "balloonText": "[[title]] of [[category]]:[[value]]",
                "fillAlphas": 1,
                "id": "AmGraph-1",
                "title": "Flights Finished",
                "type": "column",
                "valueField": "column-1"
            },
            {
                "balloonText": "[[title]] of [[category]]:[[value]]",
                "fillAlphas": 1,
                "id": "AmGraph-2",
                "title": "Flights Unfinished",
                "type": "column",
                "valueField": "column-2"
            }
        ],
        "guides": [],
        "valueAxes": [
            {
                "id": "ValueAxis-1",
                "stackType": "regular",
                "title": "Flights"
            }
        ],
        "allLabels": [],
        "balloon": {},
        "legend": {
            "useGraphSettings": true
        },
        "titles": [
            {
                "id": "Title-1",
                "size": 15,
                "text": "Daily Flights"
            }
        ],
        "dataProvider": [
            {
                "category": "Flight Progress",
                "column-1": "1",
                "column-2": "9"
            }
        ]
    }
};

function loadGraph(graphName) {
    AmCharts.makeChart("chart2div", graphs[graphName]);
}

$(document).ready(function() {
    loadGraph("Steps");

    $(".dropdown-menu li a").click(function(){
        var selText = $(this).text();
        console.log(selText);
        $(this).parents('.btn-group').find('.dropdown-toggle').html(selText+' <span class="caret"></span>');

        if (graphs[selText] !== undefined) {
            loadGraph(selText);
        }
        else {
            $("#chart2div").html("");
            alertWarning("Invalid graph", "The graph you selected does not currently exist.");
        }
    });
});
