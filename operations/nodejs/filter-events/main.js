/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
console.log("Starting KAFKA Processor in 10 seconds");
setTimeout(function () {
    var kafkaHost = process.env.STREAMZI_KAFKA_BOOTSTRAP_SERVER;
    var nodeUuid = process.env.STREAMZI_NODE_UUID;
    var sourceTopic = process.env.inputdata;
    var targetTopic = process.env.outputdata;
    var threshold = process.env.threshold;

    if(threshold === undefined){
        threshold = 0.5
    }

    console.log("Host: " + kafkaHost);
    console.log("Input topic: " + sourceTopic);
    console.log("Output topic: " + targetTopic);
    console.log("Threshold: " + threshold);

    var kafka = require("kafka-node");    
    var Producer = kafka.Producer;    
    var Consumer = kafka.Consumer;
    const client = new kafka.KafkaClient({
        kafkaHost: kafkaHost
    });
    
    
    const producer = new Producer(client);    
    producer.on("ready", function(){
        console.log("Producer ready");
    });
    
    producer.on("error", function(err){
        console.log("Producer error: " + err);
    });
    

    
    const consumer = new Consumer(
            client,
            [
                {
                    topic: sourceTopic
                }
            ], {
        autoCommit: true,
        groupId: nodeUuid
    });

    consumer.on("message", function (message) {
        var value = message.value;
        var cloudEvent = JSON.parse(value);
        var numberValue = cloudEvent.data.value;
        console.log(numberValue);
        
        if(numberValue>threshold){

            var payloads = [
                {
                    topic: targetTopic,
                    messages: message.value
                }
            ];
            producer.send(payloads, function(err, data){
            });
        }
    });
}, 10000);
