// Software Engineering I - Group I - Cloud Prototype
// File: scripts/main.js
// Author: Alex Glasser

function hideGoals() {
    $('#usergoals').slideToggle();

    $('#hideIcon').toggleClass('glyphicon-chevron-down glyphicon-chevron-up');
}

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
    // Load the content based on URL hash
    // $('.dropdownMenu1 a').on('click', function(){
    //     $(this).parent().parent().prev().html($(this).html() + '<span class="caret"></span>');
    // });

    $(".dropdownMenu1 li a").click(function(){
        $(".graphBtn:first-child").html($(this).text());
        $(".graphBtn:first-child").val($(this).text());
    });

    $(".graphBtn:first-child").html("Steps" + ' <span class="caret"></span>');
    $(".graphBtn:first-child").val("Steps");

    loadGraph();
    return; // don't feel like working on this part now. (TODO)

    // LoadHash();
    //
    // // Footer only loaded once per page load rather than every time the hash changes
    // //$("#footer").load("content/footer.html");
    //
    // // If there is a hash, use it when setting the active a href class
    // if (location.hash) {
    //     var h = location.hash;
    // }
    // // Otherwise, no hash means we are on the homepage so spoof the hash
    // else {
    //     // Also set url hash
    //     var h = location.hash = "#home";
    // }
    // // For finding the current a href in nav bar on page load
    // // http://www.grayboxpdx.com/blog/post/dynamically-add-active-class-to-links-with-jquery
    // // Note that we don't remove the other links' active class here
    // // This is because there will be no links with that class initially
    // // This also allows us to have multiple a hrefs that link to
    // //   the same page (they will all be selected here and won't
    // //   conflict with each other).
    // $("a[href='" + h + "']").each(function() {
    //     $(this).parent().addClass("active");
    // });
    //
    // // http://stackoverflow.com/a/1262939/2599996
    // // For toggling the active class on links when clicked
    // $("li a.contentlink").click(function() {
    //     $(this).closest('ul').children('li').removeClass("active");
    //     $(this).parent().addClass("active");
    // });
});

// Whenever a link is loaded, this function will hide
// the #bodytitle, #content and #extra divs and then fade
// them in with a 300ms delay
function FadeInPageContent() {
    $("#contentholder").hide();
    $("#extra").hide();

    $("#contentholder").fadeIn(300);
    $("#extra").fadeIn(300);
}

// Association dictionary for looking up which content page
// is associated with which "extra" page. The extra
// page is for the interests panel and the resources panel
// that are both shown in the #extras div.
var association = {"home": "interests",
                   "assignments": "resources",
                   "experience": "interests"};

// For converting URL hashes into title case so that they look good as headers
// Adds a method to the string class by prototyping
// Call this like:
//     "home".toProperCase() -> "Home"
// All credit goes to this SO answer:
//     http://stackoverflow.com/a/5574446/2599996
// I did not write this.
String.prototype.toProperCase = function() {
    return this.replace(/\w\S*/g, function(txt) {
        return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
    });
};

// Check if the file exists at the given file path
// This is for loading content that actually exists.
// Help/credit:
//     http://stackoverflow.com/a/16553202/2599996
//     http://stackoverflow.com/a/6854611/2599996
function FileExists(filepath) {
    // Can't return directly from the AJAX call, so keep a
    // variable for determining success
    var exists = false;
    $.ajax({
        async: false,  // want to return true/false, async should be false
        url: filepath, // argument passed to FileExists
        success: function(data) {
            exists = true; // set the variable to return
        }
    });
    return exists;
}

// Load the page based on the URL hash.
// If there is no hash, this spoofs the default to #home
// Allows for back button navigation and typed URL input.
// If the file corresponding to the URL hash does not exist,
// this spoofs the value to #sorry to display a 404-like page.
function LoadHash() {
    // If there is a hash, assign it into the h variable
    console.log("location.hash = " + location.hash);
    if (location.hash) {
        var h = location.hash;
    }
    // Otherwise, give it the default of "#home"
    else {
        // Also sets the url hash
        var h = location.hash = "#home";
    }

    // If the hash has a pound sign, remove it
    if (h.charAt(0) === '#') {
        h = h.slice(1);
    }

    var contentpath = "content/" + h + ".html";

    // Ensure that the file actually exists...
    // If not, spoof the hash to be "sorry" to load my custom error page.
    if (!FileExists(contentpath)) {
        // Don't change url hash so user can see their mistake
        h = "sorry"; // content/sorry.html is kinda like a 404 page
        contentpath = "content/" + h + ".html";
    }

    console.log(h); // debug

    // Place content/<h>.html into #content based on URL hash
    $("#contentholder").load(contentpath);

    // Now load the #extra div with the associated set of extras
    // Dictionary association is defined at the top of main.js
    extrafile = association[h];
    //console.log(extrafile);

    // Might not be in the dictionary - load only if we got something back
    if (extrafile != null) {
        // Load the extra file
        $("#extra").load("content/" + extrafile + ".html");
    }
    // If nothing came back from the lookup...
    else {
        // empty the div because nothing should be there
        $("#extra").empty();
    }

    // Fade in the content divs
    FadeInPageContent();

    // Log the URL loaded from for debugging
    //console.log("content/" + h + ".html");
}

// Offloaded the work of loading the hash content to a function
// so it can also be called in the document.ready block
// Still add the event listener
//window.addEventListener("hashchange", LoadHash);
