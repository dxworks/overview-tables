#! /usr/bin/env node
'use strict'
const {tablePicsCommand} = require("./index");

(async () => {
  tablePicsCommand.parse(process.argv)
})();
