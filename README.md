# coin-counter [![Build Status](https://travis-ci.com/AlexHff/coin-counter.svg?branch=master)](https://travis-ci.com/AlexHff/coin-counter)

Java implementation of OpenCV Coin Detection

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

To build and run this app locally you will need Java and Maven

### Quick start
Clone the repository
```bash
git clone https://github.com/AlexHff/coin-counter.git
cd coin-counter
```

Install dependencies
```bash
mvn install
```

Build and run the project
```bash
mvn clean compile exec:java -Dexec.args="path/to/images"
```

## Built With

* [OpenCV](https://opencv.org/) - Open source computer vision and machine learning software library

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
