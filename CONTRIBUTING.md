

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#how-to-contribute">How to Contribute</a>
      <ul>
        <li><a href="#standards">Standards</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#setup">Setup</a></li>
        <li><a href="#process">Process</a></li>
            <ul>
                <li><a href="#team-members">Team Members</a> </li>
                <li><a href="other-collaborators">Other Collaborators</a> </li>
            </ul>
      </ul>
    </li>
    <li><a href="#additional-information">Additional Information</a></li>
  </ol>
</details>

<!--HOW TO CONTRIBUTE-->
# How to Contribute
TBD

## Standards
Developers on this project use IntelliJ IDEA or Eclipse. You are free to use another IDE, but you'll have to know how to set it up and add any relevant directories to .gitignore.

We enforce the use of the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) through static analysis as part of our pipeline. Both Eclipse and IntelliJ IDEA have plugins for the style guide.


<!--GETTING STARTED-->
# Getting Started
TBD

## Setup
TBD

## Process
TBD

### Team Members
Team members should follow this general set of steps when working:
1. Ensure there is a ticket open on the project board for the work you want to do. If not, open one.
1. Use the ticket to create a new branch from *develop*. Note that *develop* is the default branch from which all feature branches should stem. *Main* should only be used for releases. Linking the ticket to the branch will automatically link pull requests for that branch to the ticket.
1. Move the ticket into "In Progress".
1. Perform your work, which should include the writing of test cases and ensuring they are referenced in the Maven POM file. Where appropriate, write integration tests in addition to unit tests.
1. Run unit tests locally and perform debugging.
1. Check in (final) with a pull request. This will trigger the CI/CD pipeline, automating the linting, building, and testing of your code.
1. Check the Actions report ensuring that all your tests ran and passed.
1. If you see any issues with the build and test, investigate further and open any additional tickets needed.
1. Move the ticket on the project board to "Done".

### Other Collaborators
TBD


<!--ADDITIONAL INFORMATION-->
# Additional Information
This project uses both the [Eclipse Modeling Framework](https://www.eclipse.org/modeling/emf/) (find the Javadocs [here](https://www.eclipse.org/modeling/emf/javadoc/)) and [Alloy Analyzer](https://alloytools.org/) (find the Javadocs [here](http://alloytools.org/documentation/alloy-api/index.html)). The Alloy community does answer questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/alloy), but the preferred location to interact with the community is on their [Discourse](https://alloytools.discourse.group/).