function loadGraph() {
    AmCharts.makeChart("chart2div",
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
        }
    );
}

$(document).ready(function() {
    loadGraph();    
});
