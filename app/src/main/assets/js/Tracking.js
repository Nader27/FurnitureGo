var defaultScaleValue = 0.045;
var defaultRotationValue = 0;

var rotationValues = [];
var scaleValues = [];
var oldscalex;
var oldscaley;
var oldscalez;
var allCurrentModels = [];
var allModelImgSources = [];

var deleteObj = false;

var oneFingerGestureAllowed = false;

// this global callback can be utilized to react on the transition from and to 2
// finger gestures; specifically, we disallow the drag gesture in this case to ensure an
// intuitive experience
AR.context.on2FingerGestureStarted = function () {
    oneFingerGestureAllowed = false;
};
var Touchposition;
$(document).ready(function () {
    $(document).on("touchmove", function (event) {
        if (event.originalEvent.touches && event.originalEvent.touches.length) {
            Touchposition = event.originalEvent.touches[0];
        } else if (event.originalEvent.changedTouches && event.originalEvent.changedTouches.length) {
            Touchposition = event.originalEvent.changedTouches[0];
        }
    });
});

var World = {
        modelPaths: [],
        /*
         requestedModel is the index of the next model to be created. This is necessary because we have to wait one frame in order to pass the correct initial position to the newly created model.
         initialDrag is a boolean that serves the purpose of swiping the model into the scene. In the moment that the model is created, the drag event has already started and will not be caught by the model, so the motion has to be carried out by the tracking plane.
         lastAddedModel always holds the newest model in allCurrentModels so that the plane knows which model to apply the motion to.
         */
        requestedModel: -1,
        initialDrag: false,
        lastAddedModel: null,

        /*    init: function initFn() {
         $("#inputs").empty();
         for(var i=0;i<allModelImgSources.length;i++){
         $("#inputs").append("<input data-id=" + i + " class='tracking-model-button list-group-item' type='image' src="+allModelImgSources[i]+" />");
         }
         $("#inputs").append("<input id='tracking-model-reset-button' class='tracking-model-button list-group-item' type='image' src='assets/buttons/trash.png' onclick='World.resetModels()' />");

         this.createOverlays();
         },*/

        createOverlays: function createOverlaysFn() {
            var crossHairsRedImage = new AR.ImageResource("assets/crosshairs_red.png");
            var crossHairsRedDrawable = new AR.ImageDrawable(crossHairsRedImage, 1.0);

            var crossHairsBlueImage = new AR.ImageResource("assets/crosshairs_blue.png");
            var crossHairsBlueDrawable = new AR.ImageDrawable(crossHairsBlueImage, 1.0);

            this.tracker = new AR.InstantTracker({
                onChangedState: function onChangedStateFn(state) {
                    // react to a change in tracking state here
                },
                deviceHeight: 1.0,
                onError: function (errorMessage) {
                    alert(errorMessage);
                }
            });

            this.instantTrackable = new AR.InstantTrackable(this.tracker, {
                drawables: {
                    cam: crossHairsBlueDrawable,
                    initialization: crossHairsRedDrawable
                },
                onTrackingStarted: function onTrackingStartedFn() {
                    // do something when tracking is started (recognized)
                },
                onTrackingStopped: function onTrackingStoppedFn() {
                    // do something when tracking is stopped (lost)
                },
                onTrackingPlaneClick: function onTrackingPlaneClickFn(xPos, yPos) {
                    // react to a the tracking plane being clicked here
                },
                onTrackingPlaneDragBegan: function onTrackingPlaneDragBeganFn(xPos, yPos) {
                    oneFingerGestureAllowed = true;
                    World.updatePlaneDrag(xPos, yPos);
                },
                onTrackingPlaneDragChanged: function onTrackingPlaneDragChangedFn(xPos, yPos) {
                    World.updatePlaneDrag(xPos, yPos);
                },
                onTrackingPlaneDragEnded: function onTrackingPlaneDragEndedFn(xPos, yPos) {
                    World.updatePlaneDrag(xPos, yPos);
                    World.initialDrag = false;
                },
                onError: function (errorMessage) {
                    alert(errorMessage);
                }
            });

            World.setupEventListeners()
        },

        setupEventListeners: function setupEventListenersFn() {

            $('.tracking-model-button').on('touchstart', function () {
                World.requestedModel = $(this).data("id");
            });
        },

        updatePlaneDrag: function updatePlaneDragFn(xPos, yPos) {
            if (World.requestedModel >= 0) {
                World.addModel(World.requestedModel, xPos, yPos);
                World.requestedModel = -1;
                World.initialDrag = true;
            }

            if (World.initialDrag && oneFingerGestureAllowed) {
                lastAddedModel.translate = {x: xPos, y: yPos};
            }
        },

        changeTrackerState: function changeTrackerStateFn() {

            if (this.tracker.state === AR.InstantTrackerState.INITIALIZING) {

                $("#sidebar").show();
                $("#tracking-screenshot-button").show();
                document.getElementById("tracking-start-stop-button").src = "assets/buttons/stop.png";
                document.getElementById("tracking-height-slider-container").style.visibility = "hidden";

                this.tracker.state = AR.InstantTrackerState.TRACKING;
            } else {

                $("#sidebar").hide();
                $("#tracking-screenshot-button").hide();
                document.getElementById("tracking-start-stop-button").src = "assets/buttons/start.png";
                document.getElementById("tracking-height-slider-container").style.visibility = "visible";

                this.tracker.state = AR.InstantTrackerState.INITIALIZING;
            }
        },

        changeTrackingHeight: function changeTrackingHeightFn(height) {
            this.tracker.deviceHeight = parseFloat(height);
        },

        addModel: function addModelFn(pathIndex, xpos, ypos) {
            if (World.isTracking()) {
                var modelIndex = rotationValues.length;
                World.addModelValues();

                var model = new AR.Model(World.modelPaths[pathIndex], {
                        scale: {
                            x: defaultScaleValue,
                            y: defaultScaleValue,
                            z: defaultScaleValue
                        },
                        translate: {
                            x: xpos,
                            y: ypos
                        },
                        // We recommend only implementing the callbacks actually needed as they will
                        // cause calls from native to JavaScript being invoked. Especially for the
                        // frequently called changed callbacks this should be avoided. In this
                        // sample all callbacks are implemented simply for demonstrative purposes.
                        onDragBegan: function (x, y) {
                            $("#tracking-start-stop-button").attr('src', 'assets/buttons/trash.png');
                            $("#sidebar").hide();
                            $("#tracking-screenshot-button").hide();
                            oneFingerGestureAllowed = true;
                        },
                        onDragChanged: function (relativeX, relativeY, intersectionX, intersectionY) {
                            if (oneFingerGestureAllowed) {
                                // We recommend setting the entire translate property rather than
                                // its individual components as the latter would cause several
                                // call to native, which can potentially lead to performance
                                // issues on older devices. The same applied to the rotate and
                                // scale property
                                this.translate = {x: intersectionX, y: intersectionY};
                                var off = $("#tracking-start-stop-button").offset();
                                var wid = $("#tracking-start-stop-button").width();
                                var heig = $("#tracking-start-stop-button").height();
                                var right = off.left + wid;
                                var bottom = off.top + heig;
                                if (Touchposition.pageX > off.left && Touchposition.pageX < right && Touchposition.pageY > off.top && Touchposition.pageY < bottom) {
                                    if (deleteObj == false) {
                                        deleteObj = true;
                                        oldscalex = this.scale.x;
                                        oldscaley = this.scale.y;
                                        oldscalez = this.scale.z;
                                        $("#tracking-start-stop-button").attr('src', 'assets/buttons/opentrash.png');
                                        $(this.scale).animate({x: 0.01, y: 0.01, z: 0.01});
                                    }
                                } else {
                                    if (deleteObj == true) {
                                        deleteObj = false;
                                        $("#tracking-start-stop-button").attr('src', 'assets/buttons/trash.png');
                                        $(this.scale).animate({x: oldscalex, y: oldscaley, z: oldscalez});
                                    }
                                }
                            }
                        },
                        onDragEnded: function (x, y) {
                            if (deleteObj == true) {
                                World.removeModel(this);
                            }
                            $("#tracking-start-stop-button").attr('src', 'assets/buttons/stop.png');
                            $("#sidebar").show();
                            $("#tracking-screenshot-button").show();
                            // react to the drag gesture ending
                        },
                        onRotationBegan: function (angleInDegrees) {
                            // react to the rotation gesture beginning
                            $("#sidebar").hide();
                            $("#tracking-screenshot-button").hide();
                        }
                        ,
                        onRotationChanged: function (angleInDegrees) {
                            this.rotate.z = rotationValues[modelIndex] - angleInDegrees;
                        }
                        ,
                        onRotationEnded: function (angleInDegrees) {
                            rotationValues[modelIndex] = this.rotate.z
                            $("#sidebar").show();
                            $("#tracking-screenshot-button").show();
                        }
                        ,
                        onScaleBegan: function (scale) {
                            // react to the scale gesture beginning
                            $("#sidebar").hide();
                            $("#tracking-screenshot-button").hide();
                        }
                        ,
                        onScaleChanged: function (scale) {
                            var scaleValue = scaleValues[modelIndex] * scale;
                            this.scale = {x: scaleValue, y: scaleValue, z: scaleValue};
                        }
                        ,
                        onScaleEnded: function (scale) {
                            scaleValues[modelIndex] = this.scale.x;
                            $("#sidebar").show();
                            $("#tracking-screenshot-button").show();
                        }
                    }
                );

                allCurrentModels.push(model);
                lastAddedModel = model;
                this.instantTrackable.drawables.addCamDrawable(model);
            }
        },

        isTracking: function isTrackingFn() {
            return (this.tracker.state === AR.InstantTrackerState.TRACKING);
        }
        ,

        addModelValues: function addModelValuesFn() {
            rotationValues.push(defaultRotationValue);
            scaleValues.push(defaultScaleValue);
        }
        ,

        removeModel: function removeModelFn(model) {
            this.instantTrackable.drawables.removeCamDrawable(model);
        }
        ,
        resetModels: function resetModelsFn() {
            if (confirm('Are you sure you want to Delete All Models?')) {
                for (var i = 0; i < allCurrentModels.length; i++) {
                    this.instantTrackable.drawables.removeCamDrawable(allCurrentModels[i]);
                }
                allCurrentModels = [];
                World.resetAllModelValues();
            } else {
                // Do nothing!
            }
        }
        ,

        resetAllModelValues: function resetAllModelValuesFn() {
            rotationValues = [];
            scaleValues = [];
        }
        ,

        loadPathFromJsonData: function loadPathFromJsonDataFn(paths) {
            // empty list of visible markers
            World.modelPaths = [];
            allModelImgSources = [];
            for (var i = 0; i < paths.length; i++) {
                World.modelPaths.push(paths[i].model);
                allModelImgSources.push(paths[i].image);

            }
            $('#sidebar').hide();
            $("#inputs").empty();
            for (var i = 0; i < allModelImgSources.length; i++) {
                $("#inputs").append("<input data-id=" + i + " class='tracking-model-button list-group-item' type='image' src=" + allModelImgSources[i] + " />");
            }
            $("#inputs").append("<input id='tracking-model-reset-button' class='tracking-model-button list-group-item' type='image' src='assets/buttons/delete_all.png' onclick='World.resetModels()' />");
            this.createOverlays();
        }

        ,

        captureScreen: function captureScreenFn() {
            AR.platform.sendJSONObject({
                action: "capture_screen"
            });
        }
    }
;

//World.init();
