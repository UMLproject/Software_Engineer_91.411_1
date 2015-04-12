// Software Engineering I - Group I - Cloud Prototype
// File: scripts/main.js
// Author: Alex Glasser

function hideGoals() {
    $('#usergoals').slideToggle();

    $('#hideIcon').toggleClass('glyphicon-chevron-down glyphicon-chevron-up');
}

function unimplementedFeature() {
    alertInfo("Unimplemented", "The feature you tried to use is unimplemented.");
}

$(document).ready(function() {
    // Load the content based on URL hash
    // $('.dropdownMenu1 a').on('click', function(){
    //     $(this).parent().parent().prev().html($(this).html() + '<span class="caret"></span>');
    // });

    $(".graphBtn:first-child").html("Steps" + ' <span class="caret"></span>');
    $(".graphBtn:first-child").val("Steps");

    $(".unimplementedFeature").click(function() {
        unimplementedFeature();
    });

    LoadHash();

    // If there is a hash, use it when setting the active a href class
    if (location.hash) {
        var h = location.hash;
    }
    // Otherwise, no hash means we are on the homepage so spoof the hash
    else {
        // Also set url hash
        var h = location.hash = "signup";
    }
    // For finding the current a href in nav bar on page load
    // http://www.grayboxpdx.com/blog/post/dynamically-add-active-class-to-links-with-jquery
    // Note that we don't remove the other links' active class here
    // This is because there will be no links with that class initially
    // This also allows us to have multiple a hrefs that link to
    //   the same page (they will all be selected here and won't
    //   conflict with each other).
    $("a[href='" + h + "']").each(function() {
        $(this).parent().addClass("active");
    });

    // http://stackoverflow.com/a/1262939/2599996
    // For toggling the active class on links when clicked
    $("li a.contentlink").click(function() {
        $(this).closest('ul').children('li').removeClass("active");
        $(this).parent().addClass("active");
    });
});

// Whenever a link is loaded, this function will hide
// the #bodytitle, #content and #extra divs and then fade
// them in with a 300ms delay
function FadeInPageContent() {
    $("#placeholder").hide();

    $("#placeholder").fadeIn(300);
}

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
    // console.log("location.hash = " + location.hash);
    if (location.hash) {
        var h = location.hash;
    }
    // Otherwise, give it the default of "#home"
    else {
        // Also sets the url hash
        var h = "signup";
    }

    // If the hash has a pound sign, remove it
    if (h.charAt(0) === '#') {
        h = h.slice(1);
    }

    var contentpath = h + ".html";

    // console.log(h); // debug
    // console.log(contentpath);

    // Place content/<h>.html into #content based on URL hash
    $("#placeholder").load(contentpath);

    // Fade in the content divs
    // FadeInPageContent();

    // Log the URL loaded from for debugging
    // console.log("content/" + h + ".html");
}

// Offloaded the work of loading the hash content to a function
// so it can also be called in the document.ready block
// Still add the event listener
window.addEventListener("hashchange", LoadHash);
