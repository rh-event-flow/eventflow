#Start from a basic ubuntu image
FROM docker.io/node:8.11
WORKDIR /home/node
COPY package*.json ./
RUN npm install
COPY . .
CMD [ "npm" , "start" ]