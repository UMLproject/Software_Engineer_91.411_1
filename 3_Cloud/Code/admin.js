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
		var labs = "<div class=\"form-group\"><label>" + name + "</label>"
		var buttons = createButtons(name);
		var content = document.getElementById("usercontrolform").innerHTML;
		content = content + labs + buttons;
		document.getElementById("usercontrolform").innerHTML = content;
	}	
}

function createButtons(name)
{
	var set = "<button type=\"button\" onclick=\"setGoals('" + name + "')\">Set Goals</button>";
	var edit = "<button type=\"button\" onclick=\"editProfile('" + name + "')\">Edit Profile</button>";
	var del = "<button type=\"button\" onclick=\"deleteUser('" + name + "')\">Delete</button>";

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
		alert("User " + user + " was not deleted");
	} else
	{
		alert("User " + user + " was deleted");
	}
}
