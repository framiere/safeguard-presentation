FROM node:10
RUN apt update 
RUN apt install -y fish && chsh -s /usr/bin/fish
COPY ./ .
RUN npm install && npm rebuild node-pty --update-binary
ENTRYPOINT npm start
