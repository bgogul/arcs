FROM ubuntu:xenial

# Install programs for runtime use (e.g. by Bazel, Node-GYP)
RUN apt-get update && apt-get upgrade -y && apt-get install -y \
      apt-transport-https \
      bash \
      ca-certificates \
      curl \
      g++ \
      git \
      lbzip2 \
      make \
      nodejs \
      openjdk-8-jdk \
      python \
      unzip \
      xz-utils

# Install Chrome for Selenium
#  Reference: https://tecadmin.net/setup-selenium-chromedriver-on-ubuntu/
RUN curl -sS -o - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add
RUN echo "deb [arch=amd64]  http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list
RUN apt-get -y update && apt-get -y install google-chrome-stable
# Install Selenium
RUN curl https://chromedriver.storage.googleapis.com/2.41/chromedriver_linux64.zip -o  /usr/bin/chromedriver_linux64.zip
RUN unzip /usr/bin/chromedriver_linux64.zip -d /usr/bin/chromedriver
RUN chmod +x /usr/bin/chromedriver

# Set up workspace
ENV WORKSPACE /usr/src/app
RUN mkdir -p $WORKSPACE
WORKDIR $WORKSPACE

# Install ktlint
RUN (cd /usr/bin/ && curl -L -s -O https://github.com/pinterest/ktlint/releases/download/0.35.0/ktlint && cd -)
RUN chmod +x /usr/bin/ktlint

# Install Nodejs & npm
RUN curl -sL https://deb.nodesource.com/setup_10.x | bash -
RUN apt-get install -y nodejs

# Install Android SDK
ENV ANDROID_HOME "/sdk"
COPY tools/android-sdk-packages.txt tools/android-sdk-packages.txt
COPY tools/install-android-sdk tools/install-android-sdk
COPY tools/logging.sh tools/logging.sh
RUN tools/install-android-sdk ${ANDROID_HOME}

# Allows running script with privileged permission
# i.e. scripts {...} at package.json
RUN npm set unsafe-perm true

# Install npm packages
COPY concrete-storage/package.json concrete-storage/package.json
RUN (cd concrete-storage && npm install)
COPY package.json package.json
COPY tools tools
COPY config config
COPY devtools devtools
RUN npm install

# Fetch external Bazel artifacts.
# Copy over the WORKSPACE file, everything it imports, and bazelisk.
COPY tools/bazelisk* tools/
COPY build_defs/emscripten build_defs/emscripten
COPY build_defs/kotlin_native build_defs/kotlin_native
COPY emscripten_cache emscripten_cache
COPY .bazelignore \
     .bazelversion \
     WORKSPACE \
     BUILD.bazel \
     ./
RUN ./tools/bazelisk sync

# Copy the contents of the working dir. After this the image should be ready for
# use.
COPY . .
