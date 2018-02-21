var w = window.innerWidth - 20,
    h = window.innerHeight - 100,
    margin = {top: 20, right: 20, bottom: 25, left: 25},
    radius = 8;

var svg = d3.select("#plotSvg").attr({
    width: w,
    height: h
});

d3.select("#controls").attr({
    width: w,
    height: 60
});

var dataset = [];

var datasetInSteps = [];
var pointerToSteps = 0;

// We're passing in a function in d3.max to tell it what we're maxing (x value)
var xScale = d3.scale.linear()
    .domain([0, 100])
    .range([margin.left, w - margin.right]);  // Set margins for x specific

// We're passing in a function in d3.max to tell it what we're maxing (y value)
var yScale = d3.scale.linear()
    .domain([100, 0])
    .range([margin.top, h - margin.bottom]);  // Set margins for y specific

// Add a X and Y Axis (Note: orient means the direction that ticks go, not position)
var xAxis = d3.svg.axis().scale(xScale).orient("bottom");
var yAxis = d3.svg.axis().scale(yScale).orient("left");

var circleAttrs = {
    cx: function (d) {
        return xScale(d.x);
    },
    cy: function (d) {
        return yScale(d.y);
    },
    r: radius
};

var polyline1Attrs = {
    points: function (d) {
        return "" + xScale(d.x) + ", " + yScale(d.y - 2) + " " + xScale(d.x) + ", " + (yScale(d.y + 2));
    }
};

var polyline2Attrs = {
    points: function (d) {
        return xScale(d.x - 1.5) + ", " + (yScale(d.y)) + " " + xScale(d.x + 1.5) + ", " + (yScale(d.y));
    }
};


// Adds X-Axis as a 'g' element
svg.append("g").attr({
    "class": "axis",  // Give class so we can style it
    transform: "translate(" + [0, h - margin.bottom] + ")"  // Translate just moves it down into position (or will be on top)
}).call(xAxis);  // Call the xAxis function on the group

// Adds Y-Axis as a 'g' element
svg.append("g").attr({
    "class": "axis",
    transform: "translate(" + [margin.left, 0] + ")"
}).call(yAxis);  // Call the yAxis function on the group

svg.selectAll("circle")
    .data(dataset)
    .enter()
    .append("circle")
    .attr(circleAttrs)  // Get attributes from circleAttrs var
    .on("mouseover", handleMouseOver)
    .on("mouseout", handleMouseOut);

// On Click, we want to add data to the array and chart
svg.on("click", function () {
    var coords = d3.mouse(this);

    // Normally we go from data to pixels, but here we're doing pixels to data
    var newData = {
        x: Math.round(xScale.invert(coords[0])),  // Takes the pixel number to convert to number
        y: Math.round(yScale.invert(coords[1]))
    };

    dataset.push(newData);   // Push data to our array

    svg.selectAll("circle")  // For new circle, go through the update process
        .data(dataset)
        .enter()
        .append("circle")
        .attr(circleAttrs)  // Get attributes from circleAttrs var
        .on("mouseover", handleMouseOver)
        .on("mouseout", handleMouseOut);
});

d3.selectAll("#forwardCircle").on("click", function () {
    if (pointerToSteps < (datasetInSteps.length - 1)) {
        svg.selectAll("circle")  // For new circle, go through the update process
            .remove();
        pointerToSteps++;
        repaintChart();
    }
});

d3.selectAll("#backCircle").on("click", function () {
    if (pointerToSteps > 0) {
        svg.selectAll("circle")  // For new circle, go through the update process
            .remove();
        pointerToSteps--;
        repaintChart();
    }
});


d3.selectAll("#sendCircle").on("click", function () {
    d3.xhr("computeKMeans")
        .header("Content-Type", "application/json")
        .post(JSON.stringify(dataset), function (error, newData) {
            datasetInSteps = JSON.parse(newData.response);   // Push data to our array
            console.log(datasetInSteps);
            pointerToSteps = 1;
            repaintChart();
        });
});

// Create Event Handlers for mouse
function handleMouseOver(d, i) {  // Add interactivity

    // Use D3 to select element, change color and size
    d3.select(this).attr({
        fill: "orange",
        r: radius * 2
    });

    // Specify where to put label of text
    svg.append("text").attr({
        id: "t" + d.x + "-" + d.y + "-" + i,  // Create an id for text so we can select it later for removing on mouseout
        x: function () {
            return xScale(d.x) - 30;
        },
        y: function () {
            return yScale(d.y) - 15;
        }
    })
        .text(function () {
            return [d.x, d.y];  // Value of the text
        });
}

function handleMouseOut(d, i) {
    // Use D3 to select element, change color back to normal
    d3.select(this).attr({
        fill: "black",
        r: radius
    });

    // Select text by id and then remove
    d3.select("#t" + d.x + "-" + d.y + "-" + i).remove();  // Remove text location
}

function repaintChart() {
    svg.selectAll("circle")  // For new circle, go through the update process
        .remove();
    svg.selectAll("polyline")  // For new circle, go through the update process
        .remove();

    var colors = ["red", "green", "blue"];

    for (i = 0; i < datasetInSteps[pointerToSteps].length; i++) {

        svg.selectAll("#circle" + i)
            .data(datasetInSteps[pointerToSteps][i])
            .enter()
            .append("circle")
            .attr(circleAttrs)  // Get attributes from circleAttrs var
            .attr({
                fill: colors[i]
            })
            .on("mouseover", handleMouseOver)
            .on("mouseout", handleMouseOut);

        svg.selectAll("#polylineV" + i)
            .data([datasetInSteps[pointerToSteps][i][0]])
            .enter()
            .append("polyline")
            .attr(polyline1Attrs)  // Get attributes from circleAttrs var
            .attr({
                stroke: colors[i],
                "stroke-width": 5
            });

        svg.selectAll("#polylineH" + i)
            .data([datasetInSteps[pointerToSteps][i][0]])
            .enter()
            .append("polyline")
            .attr(polyline2Attrs)  // Get attributes from circleAttrs var
            .attr({
                stroke: colors[i],
                "stroke-width": 5
            });

    }


    var multiplier = 400 / (datasetInSteps.length - 1);

    console.log(450 + pointerToSteps * multiplier);

    d3.selectAll("#progressLine")
        .attr({
            x1: 450 + pointerToSteps * multiplier,
            x2: 450 + pointerToSteps * multiplier,
            y1: 15, y2: 35
        });
}
