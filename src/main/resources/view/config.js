import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { TooltipModule } from './tooltip-module/TooltipModule.js';
import { ToggleModule } from './toggle-module/ToggleModule.js'

export const playerColors = [
  '#ff0000',
  '#49cc35'
];

export const modules = [
	GraphicEntityModule,
	TooltipModule,
	ToggleModule
];

// The list of toggles displayed in the options of the viewer
export const options = [
  ToggleModule.defineToggle({
    // The name of the toggle
    // replace "myToggle" by the name of the toggle you want to use
    toggle: 'distances',
    // The text displayed over the toggle
    title: 'Distances',
    // The labels for the on/off states of your toggle
    values: {
      'TOGGLED ON': true,
      'TOGGLED OFF': false
    },
    // Default value of your toggle
    default: false
  }),
  ToggleModule.defineToggle({
        // The name of the toggle
        // replace "myToggle" by the name of the toggle you want to use
        toggle: 'variables',
        // The text displayed over the toggle
        title: 'Variables',
        // The labels for the on/off states of your toggle
        values: {
          'TOGGLED ON': true,
          'TOGGLED OFF': false
        },
        // Default value of your toggle
        default: false
      })
]