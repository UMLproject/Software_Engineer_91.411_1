var text = '{ "USERS" : [' +
	'{ "firstName":"John" , "lastName":"Doe" },' +
	'{ "firstName":"Jane" , "lastName":"Doe" },' +
	'{ "firstName":"Bill" , "lastName":"Smith" },' +
	'{ "firstName":"Alan" , "lastName":"Clark" },' +
	'{ "firstName":"Jack" , "lastName":"McPhee" },' +
	'{ "firstName":"Chris" , "lastName":"Charles" },' +
	'{ "firstName":"Keith" , "lastName":"Wu" } ]}';

function displayData()
{
	var obj = JSON.parse(text);
	var len = Object.keys(obj.USERS).length;
	for (i=0;i<len;i++)
	{
		var name = obj.USERS[i].firstName + " " + obj.USERS[i].lastName;
		var labs = "<div id=\"" + name.replace(/\s/g, '') + "\" class=\"form-group row userRow\"><label class=\"username col-md-3\">" + name + "</label>"
		var buttons = createButtons(name);
		var content = document.getElementById("usercontrolform").innerHTML;
		content = content + labs + buttons;
		document.getElementById("usercontrolform").innerHTML = content;
	}
}

$(document).ready(function() {
	// displayData();
});

function createButtons(name)
{
	var set = "";
	set += "<div class=\"col-md-3\">";
	set += "<button class=\"adminBtn\" type=\"button\" onclick=\"setGoals('" + name + "')\">Set Goals</button>";
	set += "</div>"

	var edit = "";
	edit += "<div class=\"col-md-3\">";
	edit += "<button class=\"adminBtn\"type=\"button\" onclick=\"editProfile('" + name + "')\">Edit Profile</button>";
	edit += "</div>";

	var del = "";
	del += "<div class=\"col-md-3\">";
	del += "<button class=\"adminBtn\"type=\"button\" onclick=\"deleteUser('" + name + "')\">Delete</button>";
	del += "</div>";

	var ret = set + edit + del + "<br>";
	return ret;
}

function validateForm()
{
	var x = document.forms[""]
}

function setGoals(user)
{
	window.location.href="setgoals.html?user=" + user;
}

function editProfile(user)
{
	window.location.href="editprofile.html?user=" + user;
}

function deleteUser(user)
{
	var del = prompt("To delete the user " + user + ", please type \"DELETE\" in the field below.");
	if (del != 'DELETE')
	{
		// alert("User " + user + " was not deleted");
		alertWarning("Info", "User " + user + " was not deleted");
	}
	else
	{
		var s = '#' + user.replace(/\s/g, '');
		console.log(s);
		$(s).remove();

		// alert("User " + user + " was deleted");
		alertSuccess("Success", "User " + user + " was deleted.");
	}
}
