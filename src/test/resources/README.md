<!--
*** Based on the Best-README-Template commit 803567b0bf9d6cb929adccb22bb02acebabc44f5 obtained from:
*** https://github.com/othneildrew/Best-README-Template
-->



<!-- PROJECT LOGO -->
<br />
  <h1 align="center">Verifying executability of SysML behavior models using Alloy Analyzer</h1>
        <p align="center">
This repositority provides Alloy Analyzer code used during the development 
of NIST Interagency Report (NISTIR) 8388. The report presents an approach to 
verifying executability of system behavior models by treating them as logical 
constraint problems solved using Alloy Analyzer, a non-proprietary software 
tool supporting a textual language for logical constraints and underlying solvers.
    <br />
    <a href="https://doi.org/10.6028/NIST.IR.8388">View Report</a>
</p>

<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li>
      <a href=#about-the-project>About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
        <li><a href="#configuration-information">Configuration Information</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgements">Acknowledgements</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

With behavior models interpreted
as constraints on execution order, Alloy can determine whether the models
are executable by attempting to find executions that meet those constraints.
The approach relies on a logical interpretation of behavior modeling in the
Systems Modeling Language by Ontological Behavior Modeling method that
unifies SysML behavior modeling based on its classification elements. The
paper proposes translation between classification versions of SysML behavioral
models and logical constructs in Alloy. Finally, the approach is demonstrated
by translating and solving example SysML behavior models.

### Built With

* [Alloy Analyzer v5.1][alloy-release-url]

### Configuration Information
| Attribute | Value |
|-----------|-------|
|OS Name | Microsoft Windows 10 Enterprise |
|OS Version | 10.0.19041 N/A Build 19041 |
|OS Manufacturer | Microsoft Corporation |
|OS Configuration | Member Workstation |
|OS Build Type | Multiprocessor Free |
|System Manufacturer | HP |
|System Model | HP ZBook 14u G6 |
|System Type | x64-based PC |
|Processor(s) | 1 Processor(s) Installed. <br>[01]: Intel64 Family 6 Model 142 Stepping 12 GenuineIntel ~1792 Mhz |
|BIOS Version | HP R70 Ver. 01.08.01, 1/8/2021 |
|Total Physical Memory | 32,574 MB |
|Virtual Memory | Max Size: 37,438 MB |

<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

System requirements as specified in the 
[Alloy README](https://github.com/AlloyTools/org.alloytools.alloy/blob/master/README.md)

### Installation

1. Install [Alloy Analyzer][alloy-release-url]
    1. The documentation for Alloy can be found at
       [Alloy documentation](http://alloytools.org/documentation.html)
    1. There is an excellent readthedocs on the basics of using Alloy, written by Hillel Wayne
       at [Alloy readthedocs](https://alloy.readthedocs.io/en/latest/index.html)
1. Download the Alloy code from the NIST repo at [NISTIR 8388 repository][nist-repo-url] and unzip
    it to a local folder. Note: keeping the Alloy binary and the NIST code near each other in your 
   file is strongly recommended to ease navigation to the files.
   

<!-- USAGE EXAMPLES -->
## Usage

The code files that correspond to the NISTIR 8388 examples are numbered according to the 
associated section of the NISTIR. For example, the Alloy file "4.1.2 LoopsExamples.als"
contains a model that can produce the solutions shown in section 4.1.2 of the NISTIR.

In the Alloy Visualizer, go to **Theme -> Load Theme** and select one of the provided
theme files to view the Alloy solutions as they appear in NISTIR 8388. A brief description
of each view is provided below:

| View File Name | Description of Tuples Shown | View Description |
|---|---|---|
| 4 Examples (HappensBefore).thm | HappensBefore: solid, bold line | Focuses on HappensBefore only. Does not provide a view of composition, so any containing behaviors are not shown. |
| 4 Examples (happensDuring).thm | HappensBefore: solid, bold line<br><br>HappensDuring: dashed line | Provides a temporal view of both sequencing and composition. |
| 4 Examples (Input-Output).thm | HappensBefore: solid, bold line<br><br>Input: attribute<br><br>Output: attribute | Provides a temporal view of sequencing with a focus on the inputs and outputs of occurrences. |
| 4 Examples (SpecificInput-Output).thm | Input: dotted line<br><br>Output: dashed line<br><br>Specific subsets of Input/Output: attribute | Shows both the supersets of Input/Output and the specific subsets. |
| 4 Examples (SpecificSteps).thm | HappensBefore: solid, bold line<br><br>Specific subsets of Step: dotted line | Provides a temporal view of sequencing along with composition according to the specific subsets of the Step relation. |
| 4 Examples (Steps).thm | HappensBefore: solid, bold line<br><br>Step: dotted line | Provides a temporal view of sequencing along with composition via the Step relation. |
| 4 Examples (Summary).thm | HappensBefore: solid, bold line<br><br>Step: dotted line<br><br>Input/Output/Item: attribute | Provides an overview of the model, showing composition via the Step relation, temporal sequencing via HappensBefore, the Inputs/Outputs of each process step as attributes, and the transferred Items as attributes. |
| 4 Examples (SummarySpecificSteps).thm | HappensBefore: solid, bold line<br><br>Specific subsets of Step: dotted line<br><br>Input/Output/Item: attribute | Provides an overview of the model, showing composition via subsets of the Step relation, temporal sequencing via HappensBefore, the Inputs/Outputs of each process step as attributes, and the transferred Items as attributes. |
| 4 Examples (Transfers).thm | Source: dotted line<br><br>Target: dashed line<br><br>Item: attribute | Shows the model from the viewpoint of Transfers, highlighting the Source/Target relations as links, and the Item relation as attributes. |


<!-- CONTRIBUTING -->
## Contributing

This project is not under active development. You may use and extend the code subject 
to the license listed below.


<!-- LICENSE -->
## License

See `LICENSE` for more information.

This README was based on [Best-README-Template](https://github.com/othneildrew/Best-README-Template)
and is distributed under the MIT license.

<!-- CONTACT -->
## Contact

Contact information and acknowledgements are provided in the report linked
to at the beginning of this README.



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[nist-repo-url]: https://github.com/usnistgov/mbsdaism
[alloy-release-url]: https://github.com/AlloyTools/org.alloytools.alloy/releases
[alloy-docs-url]: https://alloy.readthedocs.io/en/latest/index.html