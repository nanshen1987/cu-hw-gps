//This file was automatically generated by
//Matlab on 31-May-2009 12:11:07.
`include "cos.vh"

module cos(
    input [`COS_INPUT_RANGE]       in,
    output reg [`COS_OUTPUT_RANGE] out);

   always @(in) begin
     casez(in)
       `COS_INPUT_WIDTH'd0: out <= `COS_OUTPUT_WIDTH'd3;
       `COS_INPUT_WIDTH'd1: out <= `COS_OUTPUT_WIDTH'd3;
       `COS_INPUT_WIDTH'd2: out <= `COS_OUTPUT_WIDTH'd2;
       `COS_INPUT_WIDTH'd3: out <= `COS_OUTPUT_WIDTH'd1;
       `COS_INPUT_WIDTH'd4: out <= `COS_OUTPUT_WIDTH'd0;
       `COS_INPUT_WIDTH'd5: out <= `COS_OUTPUT_WIDTH'd5;
       `COS_INPUT_WIDTH'd6: out <= `COS_OUTPUT_WIDTH'd6;
       `COS_INPUT_WIDTH'd7: out <= `COS_OUTPUT_WIDTH'd7;
       `COS_INPUT_WIDTH'd8: out <= `COS_OUTPUT_WIDTH'd7;
       `COS_INPUT_WIDTH'd9: out <= `COS_OUTPUT_WIDTH'd6;
       `COS_INPUT_WIDTH'd10: out <= `COS_OUTPUT_WIDTH'd6;
       `COS_INPUT_WIDTH'd11: out <= `COS_OUTPUT_WIDTH'd0;
       `COS_INPUT_WIDTH'd12: out <= `COS_OUTPUT_WIDTH'd1;
       `COS_INPUT_WIDTH'd13: out <= `COS_OUTPUT_WIDTH'd2;
       `COS_INPUT_WIDTH'd14: out <= `COS_OUTPUT_WIDTH'd3;
       `COS_INPUT_WIDTH'd15: out <= `COS_OUTPUT_WIDTH'd3;
       default: out <= `COS_OUTPUT_WIDTH'hx;
     endcase
   end
endmodule